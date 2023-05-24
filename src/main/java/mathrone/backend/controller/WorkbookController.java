package mathrone.backend.controller;

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


    //workbook API
    @GetMapping("/list") // 모든 워크북 조회(Books page)
    public List<bookItem> bookList(
        @RequestParam(value = "publisher", required = false, defaultValue = "all") String publisher,
        @RequestParam(value = "sortType", required = false, defaultValue = "star") String sortType,
        @RequestParam(value = "category", required = false, defaultValue = "all") String category,
        @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum) {

        Pageable paging = PageRequest.of(pageNum - 1, 9, Sort.by("workbookId")); //page 0부터임!
        return workBookService.getBookList(paging, publisher, category, sortType);
    }


    @GetMapping("/count") // 모든 워크북 조회(Books page)
    public Long bookCount(
        @RequestParam(value = "publisher", required = false, defaultValue = "all") String publisher,
        @RequestParam(value = "category", required = false, defaultValue = "all") String category) {
        //결과의 수 반환
        return workBookService.countWorkbook(publisher, category);
    }


    @GetMapping("/summary")
    public List<bookContent> workbookList() {
        return workBookService.getWorkbookList();
    }

    @GetMapping("/try")
    public List<UserWorkbookData> getTryingList(
        HttpServletRequest request) {
        return workBookService.getTryingBook(request);
    }

    @GetMapping("/star")
    public List<UserWorkbookData> getStarList(
        HttpServletRequest request) {
        return workBookService.getStarBook(request);
    }

}
