package mathrone.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.ProblemGradeRequestDto;
import mathrone.backend.controller.dto.ProblemGradeResponseDto;
import mathrone.backend.domain.Problem;
import mathrone.backend.domain.ProblemTry;
import mathrone.backend.domain.Solution;
import mathrone.backend.domain.UserInfo;
import mathrone.backend.repository.ProblemRepository;
import mathrone.backend.repository.ProblemTryRepository;
import mathrone.backend.repository.SolutionRepository;
import mathrone.backend.repository.UserInfoRepository;
import mathrone.backend.util.TokenProviderUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final SolutionRepository solutionRepository;
    private final ProblemRepository problemRepository;
    private final UserInfoRepository userInfoRepository;
    private final ProblemTryRepository problemTryRepository;
    private final TokenProviderUtil tokenProviderUtil;
    private final RankService rankService;

    public List<ProblemGradeResponseDto> gradeProblem(
        ProblemGradeRequestDto problemGradeRequestDtoList, String accessToken){
            if(problemGradeRequestDtoList.getIsAll()) // 전체 채점일 경우
                return gradeProblemAll(problemGradeRequestDtoList, accessToken);
            else
                return gradeSolvedProblem(problemGradeRequestDtoList, accessToken);
        }


    @Transactional
    public List<ProblemGradeResponseDto> gradeProblemAll( // 전체 채점 진행
        ProblemGradeRequestDto problemGradeRequestDtoList, String accessToken) {

        Integer upScore = 0;
        // token 검증
        if (!tokenProviderUtil.validateToken(accessToken)) {
            throw new RuntimeException("Access Token 이 유효하지 않습니다.");
        }
        // access token에서 userId 가져오기
        Integer userId = Integer.parseInt(
            tokenProviderUtil.getAuthentication(accessToken).getName());


        List<ProblemGradeResponseDto> problemGradeResponseDtoList = new ArrayList<>();
        List<ProblemGradeRequestDto.problemSolve> list = problemGradeRequestDtoList.getAnswerSubmitList();
        UserInfo user = userInfoRepository.findByUserId(userId);

        for (ProblemGradeRequestDto.problemSolve problem : list) {
            Solution solutionProblem = solutionRepository.findSolutionByProblemId(
                problem.getProblemId());
            Problem registedProblem = problemRepository.findByProblemId(problem.getProblemId());
            boolean isCorrect = false;
            ProblemTry problemTry;
            Optional<ProblemTry> registedProblemTry = problemTryRepository.findAllByProblemAndUser(
                    registedProblem,
                    user);
            if (registedProblemTry.isPresent()) {
                problemTry = registedProblemTry.get();
                problemTry.setIscorrect(isCorrect);

            } else {
                problemTry = ProblemTry.builder()
                        .iscorrect(isCorrect)
                        .user(user)
                        .problem(registedProblem)
                        .build();
            }
            try {
                if (solutionProblem.getAnswer() == Integer.parseInt(problem.getSolution())) {
                    isCorrect = true;
                    upScore++;
                    problemTry.setAnswerSubmitted(Integer.parseInt(problem.getSolution()));
                }
            }
            catch(Exception e){
                problemTry.setAnswerSubmitted(null);
            }
            problemTryRepository.save(problemTry);

            problemGradeResponseDtoList.add(ProblemGradeResponseDto.builder()
                .problemId(problem.getProblemId())
                .solution(problemTry.getAnswerSubmitted())
                .answer(solutionProblem.getAnswer()).build());
        }
        rankService.setRank(userId, upScore);
        return problemGradeResponseDtoList;
    }

    @Transactional
    public List<ProblemGradeResponseDto> gradeSolvedProblem(
            ProblemGradeRequestDto problemGradeRequestDtoList, String accessToken) {

        Integer upScore = 0;
        // token 검증
        if (!tokenProviderUtil.validateToken(accessToken)) {
            throw new RuntimeException("Access Token 이 유효하지 않습니다.");
        }

        // access token에서 userId 가져오기
        Integer userId = Integer.parseInt(
                tokenProviderUtil.getAuthentication(accessToken).getName());

        List<ProblemGradeResponseDto> problemGradeResponseDtoList = new ArrayList<>();
        List<ProblemGradeRequestDto.problemSolve> list = problemGradeRequestDtoList.getAnswerSubmitList();
        UserInfo user = userInfoRepository.findByUserId(userId);

        for (ProblemGradeRequestDto.problemSolve problem : list) {
            if (problem.getSolution().equals("a")) { // 답이 'a'인거 -> 풀지 않는 문제로 채점하지 않음
                continue;
            }
            Solution solutionProblem = solutionRepository.findSolutionByProblemId(
                    problem.getProblemId());
            Problem registedProblem = problemRepository.findByProblemId(problem.getProblemId());

            boolean isCorrect = false;
            if (solutionProblem.getAnswer() == Integer.parseInt(problem.getSolution())) {
                isCorrect = true;
            }
            Optional<ProblemTry> registedProblemTry = problemTryRepository.findAllByProblemAndUser(
                    registedProblem,
                    user); // 해당 문제를 시도한 적이 있는지 확인

            if (registedProblemTry.isPresent()) {
                if(!registedProblemTry.get().isIscorrect() && isCorrect)
                    upScore++; // 이전에 틀렸던 문제를 이번에 맞았을 경우 스코어 업
                ProblemTry problemTry = registedProblemTry.get();
                problemTry.setIscorrect(isCorrect);
                problemTry.setAnswerSubmitted(Integer.parseInt(problem.getSolution()));
                problemTryRepository.save(problemTry);
            } else { // 문제를 푼 적이 없을 경우 try레포에 새로 저장
                ProblemTry problemTry = ProblemTry.builder()
                        .answerSubmitted(Integer.parseInt(problem.getSolution()))
                        .iscorrect(isCorrect)
                        .user(user)
                        .problem(registedProblem)
                        .build();

                // user.getProblemTryList().add(problemTry);
                // registedProblem.getProblemTryList().add(problemTry);
                problemTryRepository.save(problemTry);
            }

            problemGradeResponseDtoList.add(ProblemGradeResponseDto.builder()
                    .problemId(problem.getProblemId().substring(8))
                    .solution(Integer.parseInt(problem.getSolution()))
                    .answer(solutionProblem.getAnswer()).build());
        }
        rankService.setRank(userId, upScore); // redis 랭킹 점수 업데이트
        return problemGradeResponseDtoList;
    }
}
// 이미 맞은 문제를 다시 한 번 풀 경우,
