package com.rustytech.moneymanager.controller;

import com.rustytech.moneymanager.dtos.ExpenseDto;
import com.rustytech.moneymanager.exceptions.CategoryNotFoundException;
import com.rustytech.moneymanager.exceptions.ExpenseNotFoundException;
import com.rustytech.moneymanager.exceptions.UnauthorizeException;
import com.rustytech.moneymanager.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseDto> saveExpense(@RequestBody ExpenseDto expenseDto) {
        try{
            return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.addExpense(expenseDto)    );
        }catch (CategoryNotFoundException e){
            throw new CategoryNotFoundException(e.getMessage());
        }
    }


    @GetMapping
    public ResponseEntity<List<ExpenseDto>> getExpensesForCurrentUser() {
        return ResponseEntity.ok(expenseService.getCurrentMonthExpensesForCurrentUser());
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long expenseId) {
        try{
            expenseService.deleteExpense(expenseId);
            return ResponseEntity.noContent().build();
        }catch (UnauthorizeException e){
            throw new UnauthorizeException(e.getMessage());
        }catch (ExpenseNotFoundException e){
            throw new ExpenseNotFoundException(e.getMessage());
        }


    }


}
