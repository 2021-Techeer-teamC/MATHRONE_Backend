package mathrone.backend.service;


import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.problem.ProblemDto;
import mathrone.backend.domain.ChapterInfo;
import mathrone.backend.domain.Problem;
import mathrone.backend.domain.UserInfo;
import mathrone.backend.domain.WorkBookInfo;
import mathrone.backend.error.exception.CustomException;
import mathrone.backend.error.exception.ErrorCode;
import mathrone.backend.repository.ChapterRepository;
import mathrone.backend.repository.ProblemRepository;
import mathrone.backend.repository.ProblemTryRepository;
import mathrone.backend.repository.UserInfoRepository;
import mathrone.backend.repository.WorkBookRepository;
import mathrone.backend.util.TokenProviderUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProblemServiceImpl implements ProblemService {

    private final ProblemRepository problemRepository;
    private final TokenProviderUtil tokenProviderUtil;
    private final UserInfoRepository userInfoRepository;
    private final ProblemTryRepository problemTryRepository;
    private final WorkBookRepository workBookRepository;
    private final ChapterRepository chapterRepository;

    @Override
    public ProblemDto findProblemById(String problemId) {
        Problem problem = problemRepository.findById(problemId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PROBLEM));
        return new ProblemDto(problem);
    }

    @Override
    public List<ProblemDto> findProblem(String workbookId, String chapterId) {
        if (workbookId.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        List<Problem> problems;
        WorkBookInfo workBook;
        ChapterInfo chapter;

        if (chapterId.isEmpty()) {
            workBook = workBookRepository.findById(workbookId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_WORKBOOK));
            problems = problemRepository.findByWorkbookOrderByProblemId(workBook);
        } else {
            workBook = workBookRepository.findById(workbookId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_WORKBOOK));
            chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CHAPTER));
            problems = problemRepository.findByWorkbookAndChapterOrderByProblemId(workBook,
                chapter);
        }

        return problems.stream().map(ProblemDto::new).collect(
            Collectors.toList());
    }

    /**
     * 요청에 따라 유저가 시도한 문제 or 모든 유저 중 많이 시도한 문제 반환
     *
     * @param request
     * @param onlyIncorrect true => 유저가 시도한 문제 중, 정답인 문제 반환 /  false => 유저가 시도한 모든 문제 반환
     * @return List<ProblemDto>
     */
    public List<ProblemDto> getTryProblem(HttpServletRequest request, Boolean onlyIncorrect) {
        String accessToken = tokenProviderUtil.resolveToken(request);

        if (accessToken == null) {
            if (onlyIncorrect) {
                return problemRepository.findAllByProblemByUserTriedCorrect(onlyIncorrect).stream()
                    .map(
                        ProblemDto::new).collect(Collectors.toList());
            } else {
                return problemRepository.findAllByProblemByUserTried().stream().map(
                    ProblemDto::new).collect(Collectors.toList());
            }
        } else {
            UserInfo user = userInfoRepository.findById(
                Integer.parseInt(tokenProviderUtil.getAuthentication(accessToken).getName())
            ).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            if (!onlyIncorrect) {
                return problemTryRepository.findAllByUserOrderByProblem(user).stream().map(
                    a -> new ProblemDto(a.getProblem())).collect(Collectors.toList());
            } else {
                return problemTryRepository.findAllByUserAndIscorrectOrderByProblem(user, false)
                    .stream().map(
                        a -> new ProblemDto(a.getProblem())).collect(Collectors.toList());
            }
        }
    }
}