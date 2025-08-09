package com.rustytech.moneymanager.controller;

import com.rustytech.moneymanager.dtos.ExpenseDto;
import com.rustytech.moneymanager.dtos.IncomeDto;
import com.rustytech.moneymanager.exceptions.CategoryNotFoundException;
import com.rustytech.moneymanager.exceptions.ExpenseNotFoundException;
import com.rustytech.moneymanager.exceptions.UnauthorizeException;
import com.rustytech.moneymanager.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/incomes")
@RequiredArgsConstructor
public class IncomeController {
    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<IncomeDto> saveIncome(@RequestBody IncomeDto incomeDto) {
        try{
            return ResponseEntity.status(HttpStatus.CREATED).body(incomeService.addIncome(incomeDto)    );

        }catch (CategoryNotFoundException e){
            throw new CategoryNotFoundException(e.getMessage());
        }
    }



    @GetMapping
    public ResponseEntity<List<IncomeDto>> getExpensesForCurrentUser() {
        return ResponseEntity.ok(incomeService.getCurrentMonthIncomesForCurrentUser());
    }


    @DeleteMapping("/{incomeId}")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long incomeId) {
        try{
            incomeService.deleteIncome(incomeId);
            return ResponseEntity.noContent().build();
        }catch (UnauthorizeException e){
            throw new UnauthorizeException(e.getMessage());
        }catch (ExpenseNotFoundException e){
            throw new ExpenseNotFoundException(e.getMessage());
        }

    }









}
