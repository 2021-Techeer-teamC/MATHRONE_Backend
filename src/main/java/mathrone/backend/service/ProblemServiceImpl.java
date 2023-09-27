package mathrone.backend.service;


import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.ProblemDto;
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
    public Set<ProblemDto> findProblem(String workbookId, String chapterId) {
        WorkBookInfo workBook = workBookRepository.findById(workbookId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_WORKBOOK));
        ChapterInfo chapter = chapterRepository.findById(chapterId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CHAPTER));
        Set<Problem> problems = problemRepository.findByWorkbookAndChapter(workBook,
            chapter);

        return problems.stream().map(ProblemDto::new).collect(
            Collectors.toSet());
    }

    public Set<ProblemDto> getTryProblem(HttpServletRequest request, Boolean onlyIncorrect) {
        String accessToken = tokenProviderUtil.resolveToken(request);

        if (accessToken == null) {
            throw new CustomException(ErrorCode.AUTHENTICATION_ERROR);
        }

        UserInfo user = userInfoRepository.findById(
            Integer.parseInt(tokenProviderUtil.getAuthentication(accessToken).getName())
        ).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!onlyIncorrect) {
            return problemTryRepository.findAllByUser(user).stream().map(
                a -> new ProblemDto(a.getProblem())).collect(Collectors.toSet());
        } else {
            return problemTryRepository.findAllByUserAndIscorrect(user, onlyIncorrect).stream().map(
                a -> new ProblemDto(a.getProblem())).collect(Collectors.toSet());
        }
    }
}