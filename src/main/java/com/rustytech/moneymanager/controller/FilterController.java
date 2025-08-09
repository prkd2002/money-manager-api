package com.rustytech.moneymanager.controller;

import com.rustytech.moneymanager.dtos.ExpenseDto;
import com.rustytech.moneymanager.dtos.FilterDto;
import com.rustytech.moneymanager.dtos.IncomeDto;
import com.rustytech.moneymanager.service.ExpenseService;
import com.rustytech.moneymanager.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/filter")
public class FilterController {
    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<?> filterTransactions(@RequestBody FilterDto filter) {
        LocalDate safeMinDate = LocalDate.of(1970, 1, 1); // ou 0001,1,1
        LocalDate startDate = filter.getStartDate() != null ? filter.getStartDate() : safeMinDate;
        LocalDate endDate = filter.getEndDate() != null ? filter.getEndDate() : LocalDate.now();
        String keyWord = filter.getKeyWord() != null ? filter.getKeyWord() : "";
        String sortField = filter.getSortField() != null ? filter.getSortField() : "date";
        Sort.Direction direction = "desc".equalsIgnoreCase(filter.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortField);
        if("income".equalsIgnoreCase(filter.getType())){
            List<IncomeDto> incomes =  incomeService.filterIncomes(startDate, endDate, keyWord, sort);
            return ResponseEntity.ok(incomes);

        }else if("expense".equalsIgnoreCase(filter.getType())){
            List<ExpenseDto> expenses = expenseService.filterExpenses(startDate, endDate, keyWord, sort);
            return ResponseEntity.ok(expenses);
        }else{
            return ResponseEntity.badRequest().body("Invalid Type. Must be 'income' or 'expense'");
        }
    }
}
