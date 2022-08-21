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

    @Transactional
    public List<ProblemGradeResponseDto> gradeProblem(
        ProblemGradeRequestDto problemGradeRequestDtoList, String accessToken) {

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
            if (solutionProblem.getAnswer() == problem.getSolution()) {
                isCorrect = true;
            }

            Optional<ProblemTry> registedProblemTry = problemTryRepository.findAllByProblemAndUser(
                registedProblem,
                user);
            if (registedProblemTry.isPresent()) {
                ProblemTry problemTry = registedProblemTry.get();
                problemTry.setIscorrect(isCorrect);
                problemTry.setAnswerSubmitted(problem.getSolution());
            } else {
                ProblemTry problemTry = ProblemTry.builder()
                    .answerSubmitted(problem.getSolution())
                    .iscorrect(isCorrect)
                    .user(user)
                    .problem(registedProblem)
                    .build();

                // user.getProblemTryList().add(problemTry);
                // registedProblem.getProblemTryList().add(problemTry);
                problemTryRepository.save(problemTry);
            }

            problemGradeResponseDtoList.add(ProblemGradeResponseDto.builder()
                .problemId(problem.getProblemId())
                .solution(problem.getSolution())
                .answer(isCorrect).build());
        }
        return problemGradeResponseDtoList;
    }
}
