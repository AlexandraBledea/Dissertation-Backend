package ubb.dissertation.benchmark.dto;

import lombok.Data;

@Data
public class Pagination {

    private Integer pageNumber;
    private Integer pageSize;
    private Integer totalNumberOfPages;
    private Integer totalNumberOfItems;
}
