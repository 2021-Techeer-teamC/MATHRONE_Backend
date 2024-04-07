package mathrone.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.ProblemGradeRequestDto;
import mathrone.backend.controller.dto.ProblemGradeResponseDto;
import mathrone.backend.domain.Problem;
import mathrone.backend.domain.ProblemTry;
import mathrone.backend.domain.Solution;
import mathrone.backend.domain.UserInfo;
import mathrone.backend.error.exception.CustomException;
import mathrone.backend.error.exception.ErrorCode;
import mathrone.backend.repository.ProblemRepository;
import mathrone.backend.repository.ProblemTryRepository;
import mathrone.backend.repository.SolutionRepository;
import mathrone.backend.repository.UserInfoRepository;
import mathrone.backend.util.TokenProviderUtil;
import org.apache.commons.lang3.ObjectUtils.Null;
import org.apache.commons.lang3.math.NumberUtils;
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
        boolean checkAll, ProblemGradeRequestDto problemGradeRequestDtoList, HttpServletRequest request){
            if(checkAll) // 전체 채점일 경우
                return gradeProblemAll(problemGradeRequestDtoList, request);
            else
                return gradeSolvedProblem(problemGradeRequestDtoList, request);
        }


    public List<ProblemGradeResponseDto> gradeProblemAll( // 전체 채점 진행
        ProblemGradeRequestDto problemGradeRequestDtoList, HttpServletRequest request) {

        Integer upScore = 0;
        String accessToken = tokenProviderUtil.resolveToken(request);
        // token 검증
        if (!tokenProviderUtil.validateToken(accessToken, request)) {
            throw (CustomException) request.getAttribute("Exception");
        }
        // access token에서 userId 가져오기
        Integer userId = Integer.parseInt(
            tokenProviderUtil.getAuthentication(accessToken).getName());


        List<ProblemGradeResponseDto> problemGradeResponseDtoList = new ArrayList<>();  // return할 data
        List<ProblemGradeRequestDto.problemSolve> list = problemGradeRequestDtoList.getAnswerSubmitList();  // 사용자가 제출한 문제의 답
        UserInfo user = userInfoRepository.findByUserId(userId);

        for (ProblemGradeRequestDto.problemSolve problem : list) {
            boolean isCorrect = false;
            Solution solutionProblem = solutionRepository.findSolutionByProblemId(
                    problem.getProblemId());    // 실제 문제 답안 조회
            if(problem.getMyAnswer().equals("a")) {
                problemGradeResponseDtoList.add(ProblemGradeResponseDto.builder()
                        .problemId(problem.getProblemId().substring(8))
                        .correctAnswer(null)
                        .myAnswer(solutionProblem.getAnswer()).build());
            }
            else {
                isCorrect = grading(problem, solutionProblem);   // 제출한 답의 참, 거짓 여부 판별
                upScore = saveTry(problem, user, isCorrect, upScore);   // try 기록 저장 및 스코어 계산
                problemGradeResponseDtoList.add(ProblemGradeResponseDto.builder()
                        .problemId(problem.getProblemId().substring(8))
                        .correctAnswer(Integer.parseInt(problem.getMyAnswer()))
                        .myAnswer(solutionProblem.getAnswer()).build());
            }
        }
        rankService.setRank(userId, upScore);
        return problemGradeResponseDtoList;
    }

    public List<ProblemGradeResponseDto> gradeSolvedProblem(
            ProblemGradeRequestDto problemGradeRequestDtoList, HttpServletRequest request) {

        Integer upScore = 0;
        String accessToken = tokenProviderUtil.resolveToken(request);
        // token 검증
        if (!tokenProviderUtil.validateToken(accessToken, request)) {
            throw (CustomException) request.getAttribute("Exception");
        }

        // access token에서 userId 가져오기
        Integer userId = Integer.parseInt(
                tokenProviderUtil.getAuthentication(accessToken).getName());

        List<ProblemGradeResponseDto> problemGradeResponseDtoList = new ArrayList<>();
        List<ProblemGradeRequestDto.problemSolve> list = problemGradeRequestDtoList.getAnswerSubmitList();
        UserInfo user = userInfoRepository.findByUserId(userId);

        for (ProblemGradeRequestDto.problemSolve problem : list) {
            boolean isCorrect = false;
            if (problem.getMyAnswer().equals("a")) { // 답이 'a'인거 -> 풀지 않는 문제로 채점하지 않음
                continue;
            }
            else {
                Solution solutionProblem = solutionRepository.findSolutionByProblemId(
                        problem.getProblemId());    // 실제 문제 답안 조회
                isCorrect = grading(problem, solutionProblem);   // 제출한 답의 참, 거짓 여부 판별
                upScore = saveTry(problem, user, isCorrect, upScore);   // try 기록 저장 및 스코어 계산

                problemGradeResponseDtoList.add(ProblemGradeResponseDto.builder()
                        .problemId(problem.getProblemId().substring(8))
                        .correctAnswer(solutionProblem.getAnswer())
                        .myAnswer(Integer.parseInt(problem.getMyAnswer())).build());
            }
        }
        rankService.setRank(userId, upScore); // redis 랭킹 점수 업데이트
        return problemGradeResponseDtoList;
    }

    @Transactional
    public boolean grading(ProblemGradeRequestDto.problemSolve problem, Solution solutionProblem){
        if (solutionProblem.getAnswer() == Integer.parseInt(problem.getMyAnswer())) {
            return true;    // 답이 맞을 경우 참 반환
        }
        else
            return false;   // 답이 틀릴 경우 거짓 반환
    }
    @Transactional
    public int saveTry(ProblemGradeRequestDto.problemSolve problem, UserInfo user, boolean isCorrect, int upScore){
        Problem registedProblem = problemRepository.findById(problem.getProblemId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PROBLEM));

        Optional<ProblemTry> registedProblemTry = problemTryRepository.findAllByProblemAndUser(
                registedProblem,
                user); // 해당 문제를 시도한 적이 있는지 확인

        if (registedProblemTry.isPresent()) {   // 푼 기록이 있을 경우
            if(!registedProblemTry.get().isIscorrect() && isCorrect)
                upScore++; // 이전에 틀렸던 문제를 이번에 맞았을 경우 스코어 업
            ProblemTry problemTry = registedProblemTry.get();
            problemTry.setIscorrect(isCorrect);
            try {problemTry.setAnswerSubmitted(NumberUtils.toInt(problem.getMyAnswer()));}
            catch (Exception e){problemTry.setAnswerSubmitted(null);}
            problemTryRepository.save(problemTry);
        } else { // 문제를 푼 적이 없을 경우 try레포에 새로 저장
            if(isCorrect)   // 푼 기록이 없고 맞았을 경우 점수 up
                upScore++;
            ProblemTry problemTry = ProblemTry.builder()
                    .answerSubmitted(NumberUtils.toInt(problem.getMyAnswer()))
                    .iscorrect(isCorrect)
                    .user(user)
                    .problem(registedProblem)
                    .build();
            problemTryRepository.save(problemTry);
        }
        return upScore;
    }
}
// 이미 맞은 문제를 다시 한 번 풀 경우,
