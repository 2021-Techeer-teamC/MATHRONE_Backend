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
            if(problemGradeRequestDtoList.getIsAll())
                return gradeProblemAll(problemGradeRequestDtoList, accessToken);
            else
                return gradeSolvedProblem(problemGradeRequestDtoList, accessToken);
        }


    @Transactional
    public List<ProblemGradeResponseDto> gradeProblemAll(
        ProblemGradeRequestDto problemGradeRequestDtoList, String accessToken) {

        Integer upScore=0;
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

        Integer upScore=0;
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
            if (problem.getSolution().equals("a")){
                continue;
            }
            if (solutionProblem.getAnswer() == Integer.parseInt(problem.getSolution())) {
                isCorrect = true;
            }

            Optional<ProblemTry> registedProblemTry = problemTryRepository.findAllByProblemAndUser(
                    registedProblem,
                    user);

            if (registedProblemTry.isPresent()) {
                if(!registedProblemTry.get().isIscorrect() && isCorrect)
                    upScore++;
                ProblemTry problemTry = registedProblemTry.get();
                problemTry.setIscorrect(isCorrect);
                problemTry.setAnswerSubmitted(Integer.parseInt(problem.getSolution()));
                problemTryRepository.save(problemTry);
            } else {
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
        rankService.setRank(userId, upScore);
        return problemGradeResponseDtoList;
    }
}


// 이미 맞은 문제를 다시 한 번 풀 경우,
