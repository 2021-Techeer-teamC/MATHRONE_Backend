package mathrone.backend.controller;


import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import mathrone.backend.controller.dto.BookDetailDto;
import mathrone.backend.controller.dto.UserEvaluateLevelRequestDto;
import mathrone.backend.controller.dto.UserWorkbookDataInterface;
import mathrone.backend.controller.dto.WorkbookDto;
import mathrone.backend.controller.dto.interfaces.UserSolvedWorkbookResponseDtoInterface;
import mathrone.backend.domain.bookContent;
import mathrone.backend.service.WorkBookService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workbook")
public class WorkbookController {

    private final WorkBookService workBookService;

    public WorkbookController(WorkBookService workBookService) {
        this.workBookService = workBookService;
    }

    @GetMapping("/list")
    @ApiOperation(value = "workbook 조회", notes = "parameter에 따라 filtering된 workbook 리스트 가져오기")
    public ResponseEntity<List<WorkbookDto>> bookList(
        @RequestParam(value = "publisher", required = false, defaultValue = "all") String publisher,
        @RequestParam(value = "sortType", required = false, defaultValue = "like") String sortType,
        @RequestParam(value = "category", required = false, defaultValue = "all") String category,
        @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
        HttpServletRequest request) {
        Pageable paging = PageRequest.of(pageNum - 1, 9, Sort.by("workbookId")); //page 0부터임!
        return ResponseEntity.status(OK)
            .body(workBookService.getBookList(request, paging, publisher, category, sortType));
    }


    @GetMapping("/count")
    @ApiOperation(value = "workbook 개수 조회", notes = "parameter에 따라 filtering된 workbook 개수 반환")
    public ResponseEntity<Long> bookCount(
        @RequestParam(value = "publisher", required = false, defaultValue = "all") String publisher,
        @RequestParam(value = "category", required = false, defaultValue = "all") String category) {
        //결과의 수 반환
        return ResponseEntity.status(OK).body(workBookService.countWorkbook(publisher, category));
    }


    @GetMapping("/summary")
    @ApiOperation(value = "문제집 리스트 반환", notes = "publisher와 categories로 group화된 workbook 리스트 반환")
    public ResponseEntity<List<bookContent>> workbookList() {
        return ResponseEntity.status(OK).body(workBookService.getWorkbookList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDetailDto> workbookDetail(
        @PathVariable(value = "id") String bookId, HttpServletRequest request) {
        return ResponseEntity.status(OK).body(workBookService.getWorkbookDetail(bookId, request));
    }

    @GetMapping("/try")
    @ApiOperation(value = "사용자가 시도한 문제집 리스트 반환", notes = "access token가 존재하면 특정 사용자, 존재하지 않으면 모든 사용자가 시도한 문제집 리스트를 반환")
    public ResponseEntity<List<UserWorkbookDataInterface>> getTriedWorkbooks(
        HttpServletRequest request) {
        return ResponseEntity.status(OK).body(workBookService.getTriedWorkbook(request));
    }

    @GetMapping("/star")
    @ApiOperation(value = "사용자가 즐겨찾는 문제집 리스트 반환", notes = "access token가 존재하면 특정 사용자, 존재하지 않으면 모든 사용자가 즐겨찾는 문제집 리스트를 반환")
    public ResponseEntity<List<UserWorkbookDataInterface>> getStarWorkbooks(
        HttpServletRequest request) {
        return ResponseEntity.status(OK).body(workBookService.getStarWorkbook(request));
    }

    @PostMapping("/star/{workbookId}")
    @ApiOperation(value = "사용자의 특정 문제집 즐겨찾기 추가", notes = "사용자 인증 후, 문제집 즐겨찾기 처리")
    public ResponseEntity<Object> starWorkbook(
        HttpServletRequest request, @PathVariable String workbookId) {
        workBookService.starWorkbook(request, workbookId);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @DeleteMapping("/star/{workbookId}")
    @ApiOperation(value = "사용자의 특정 문제집 즐겨찾기 제거", notes = "사용자 인증 후, 문제집 즐겨찾기 처리")
    public ResponseEntity<Object> deleteStarWorkbook(
        HttpServletRequest request, @PathVariable String workbookId) {
        workBookService.deleteStarWorkbook(request, workbookId);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @GetMapping(value = {"/track/solved", "/track/solved/{workbookId}"})
    @ApiOperation(value = "유저가 푼 문제집의 풀이 tracking", notes = "유저의 token 필요, workbookId 여부에 따라 필터링된 풀이 정보 반환")
    public ResponseEntity<Set<UserSolvedWorkbookResponseDtoInterface>> trackSolvedWorkbook(
        HttpServletRequest request,
        @PathVariable(value = "workbookId", required = false) Optional<String> workbookId) {
        return ResponseEntity.status(OK)
            .body(workBookService.trackSolvedWorkbooks(request, workbookId));
    }

    @PostMapping("/level")
    @ApiOperation(value = "유저의 문제집 평가", notes = "유저 token 필요")
    public ResponseEntity<Void> evaluateWorkbook(
        HttpServletRequest request,
        @RequestBody UserEvaluateLevelRequestDto userEvaluateLevelRequestDto
    ) {
        workBookService.evaluateWorkbook(request, userEvaluateLevelRequestDto);
        return ResponseEntity.status(CREATED).build();
    }

}
