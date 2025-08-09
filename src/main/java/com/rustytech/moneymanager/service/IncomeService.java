package com.rustytech.moneymanager.service;



import com.rustytech.moneymanager.dtos.IncomeDto;
import com.rustytech.moneymanager.entity.CategoryEntity;
import com.rustytech.moneymanager.entity.IncomeEntity;
import com.rustytech.moneymanager.entity.Profile;
import com.rustytech.moneymanager.exceptions.CategoryNotFoundException;
import com.rustytech.moneymanager.exceptions.IncomeNotFoundException;
import com.rustytech.moneymanager.exceptions.UnauthorizeException;
import com.rustytech.moneymanager.repository.CategoryRepository;
import com.rustytech.moneymanager.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncomeService {
    private final ProfileService profileService;
    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;

    public IncomeDto addIncome(IncomeDto incomeDto){
        Profile profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(incomeDto.getCategoryId()).orElseThrow(()->new CategoryNotFoundException("Category not found"));
        var newIncome = toEntity(incomeDto, profile, category);
        newIncome  = incomeRepository.save(newIncome);
        return toDto(newIncome);
    }


    // Retrieve all Income for current month/based on the start date and end date
    public List<IncomeDto> getCurrentMonthIncomesForCurrentUser(){
        var profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        var incomeList = incomeRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
        return incomeList.stream().map(this::toDto).collect(Collectors.toList());

    }

    // Delete Income by id for current User
    public void deleteIncome(Long incomeId){
        var profile = profileService.getCurrentProfile();
        var income = incomeRepository.findById(incomeId).orElseThrow(() -> new IncomeNotFoundException("Expense with Id " + incomeId + " was not found !"));
        if(!income.getProfile().getId().equals(profile.getId())){
            throw new UnauthorizeException("Unauthorized to delete this Income !");
        }
        incomeRepository.delete(income);

    }

    // Get latest 5 Incomes for current User
    public List<IncomeDto>  getLatest5IncomesForCurrentUser(){
        Profile profile = profileService.getCurrentProfile();
        List<IncomeEntity> incomeList = incomeRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return incomeList.stream().map(this::toDto).collect(Collectors.toList());
    }

    // Get Total Incomes for current User
    public BigDecimal getTotalIncomeForCurrentUser(){
        var profile = profileService.getCurrentProfile();
        BigDecimal totalIncome = incomeRepository.findTotalIncomeByProfileId(profile.getId());
        return totalIncome == null ? BigDecimal.ZERO : totalIncome;
    }


    // Filter Income
    public List<IncomeDto> filterIncomes(LocalDate startDate, LocalDate endDate, String keyword, Sort sort){
        var profile = profileService.getCurrentProfile();
        List<IncomeEntity> incomeEntityList = incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), startDate, endDate, keyword, sort);
        return incomeEntityList.stream().map(this::toDto).collect(Collectors.toList());

    }
    private IncomeEntity toEntity(IncomeDto incomeDto, Profile profile, CategoryEntity category) {
        return IncomeEntity.builder()
                .name(incomeDto.getName())
                .icon(incomeDto.getIcon())
                .amount(incomeDto.getAmount())
                .category(category)
                .profile(profile)
                .date(incomeDto.getDate())
                .build();
    }

    private IncomeDto toDto(IncomeEntity incomeEntity) {
        return IncomeDto.builder()
                .id(incomeEntity.getId())
                .name(incomeEntity.getName())
                .icon(incomeEntity.getIcon())
                .categoryId(incomeEntity.getCategory() != null ? incomeEntity.getCategory().getId() : null )
                .categoryName(incomeEntity.getCategory() == null ? null: incomeEntity.getCategory().getName())
                .amount(incomeEntity.getAmount())
                .date(incomeEntity.getDate()).createdAt(incomeEntity.getCreatedAt()).updatedAt(incomeEntity.getUpdatedAt()).
                build();
    }
}
