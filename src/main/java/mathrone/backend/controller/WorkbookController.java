package mathrone.backend.controller;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import mathrone.backend.domain.UserWorkbookData;
import mathrone.backend.domain.bookContent;
import mathrone.backend.domain.bookItem;
import mathrone.backend.service.WorkBookService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
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
    public List<bookItem> bookList(
        @RequestParam(value = "publisher", required = false, defaultValue = "all") String publisher,
        @RequestParam(value = "sortType", required = false, defaultValue = "star") String sortType,
        @RequestParam(value = "category", required = false, defaultValue = "all") String category,
        @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum) {

        Pageable paging = PageRequest.of(pageNum - 1, 9, Sort.by("workbookId")); //page 0부터임!
        return workBookService.getBookList(paging, publisher, category, sortType);
    }


    @GetMapping("/count")
    @ApiOperation(value = "workbook 개수 조회", notes = "parameter에 따라 filtering된 workbook 개수 반환")
    public Long bookCount(
        @RequestParam(value = "publisher", required = false, defaultValue = "all") String publisher,
        @RequestParam(value = "category", required = false, defaultValue = "all") String category) {
        //결과의 수 반환
        return workBookService.countWorkbook(publisher, category);
    }


    @GetMapping("/summary")
    @ApiOperation(value = "문제집 리스트 반환", notes = "publisher와 categories로 group화된 workbook 리스트 반환")
    public List<bookContent> workbookList() {
        return workBookService.getWorkbookList();
    }

    @GetMapping("/try")
    @ApiOperation(value = "사용자가 시도한 문제집 리스트 반환", notes = "access token가 존재하면 특정 사용자, 존재하지 않으면 모든 사용자가 시도한 문제집 리스트를 반환")
    public List<UserWorkbookData> getTryingList(
        HttpServletRequest request) {
        return workBookService.getTryingBook(request);
    }

    @GetMapping("/star")
    @ApiOperation(value = "사용자가 즐겨찾는 문제집 리스트 반환", notes = "access token가 존재하면 특정 사용자, 존재하지 않으면 모든 사용자가 즐겨찾는 문제집 리스트를 반환")
    public List<UserWorkbookData> getStarList(
        HttpServletRequest request) {
        return workBookService.getStarBook(request);
    }

}
