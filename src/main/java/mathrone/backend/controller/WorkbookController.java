package mathrone.backend.controller;

import mathrone.backend.controller.dto.BookDetailDto;
import mathrone.backend.domain.*;
import mathrone.backend.service.WorkBookService;
//import org.apache.commons.lang3.tuple.Pair;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;//자료형 때문에 오류였음.. awt.print.Pageable아님
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;


import java.util.*;

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

    @GetMapping("workbook/chapters")
    public BookDetailDto workbookDetail(
            @RequestParam(value = "workbook") String workbook) {
        return workBookService.getWorkbookDetail(workbook);
    }

}
