package com.pickmebackend.service;

import com.pickmebackend.domain.Account;
import com.pickmebackend.domain.AccountTech;
import com.pickmebackend.domain.Technology;
import com.pickmebackend.domain.dto.account.AccountListResponseDto;
import com.pickmebackend.domain.dto.account.AccountRequestDto;
import com.pickmebackend.domain.dto.account.AccountResponseDto;
import com.pickmebackend.domain.enums.UserRole;
import com.pickmebackend.error.ErrorMessage;
import com.pickmebackend.repository.AccountRepository;
import com.pickmebackend.repository.AccountTechRepository;
import com.pickmebackend.repository.TechnologyRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import static com.pickmebackend.error.ErrorMessageConstant.USERNOTFOUND;

@Service
@RequiredArgsConstructor
public class AccountService{

    private final AccountRepository accountRepository;

    private final ModelMapper modelMapper;

    private final PasswordEncoder passwordEncoder;

    private final AccountTechRepository accountTechRepository;

    public ResponseEntity<?> getAllAccounts() {
        List<AccountResponseDto> accountResponseDtos = this.accountRepository.findAllDesc()
                .stream()
                .map(AccountResponseDto::new)
                .collect(Collectors.toList());

        return new ResponseEntity<>(accountResponseDtos, HttpStatus.OK);
    }

    @Transactional
    public AccountResponseDto saveAccount(AccountRequestDto accountDto) {
        Account account = modelMapper.map(accountDto, Account.class);
        account.setPassword(this.passwordEncoder.encode(account.getPassword()));
        account.setValue();
        Account savedAccount = this.accountRepository.save(account);

        List<Technology> technologyList = accountDto.getTechnologyList();

        if (technologyList != null) {
            technologyList.forEach(tech -> savedAccount.getAccountTechSet().add(
                    accountTechRepository.save(AccountTech.builder()
                            .account(savedAccount)
                            .technology(tech)
                            .build())));
        }

        AccountResponseDto accountResponseDto = modelMapper.map(savedAccount, AccountResponseDto.class);
        accountResponseDto.toTech(savedAccount);
        return accountResponseDto;
    }

    public AccountResponseDto updateAccount(Account account, AccountRequestDto accountDto) {
        modelMapper.map(accountDto, account);
        Account modifiedAccount = this.accountRepository.save(account);

        return modelMapper.map(modifiedAccount, AccountResponseDto.class);
    }

    public AccountResponseDto deleteAccount(Account account) {
        AccountResponseDto accountResponseDto = modelMapper.map(account, AccountResponseDto.class);
        accountRepository.delete(account);

        return accountResponseDto;
    }

    public ResponseEntity<?> getAccount(Account currentUser) {
        if (currentUser == null) {
            return new ResponseEntity<>(USERNOTFOUND, HttpStatus.BAD_REQUEST);
        }
        Optional<Account> accountOptional = accountRepository.findById(currentUser.getId());
        if (!accountOptional.isPresent()) {
            return new ResponseEntity<>(new ErrorMessage(USERNOTFOUND), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok().body(accountOptional.get());
    }

    public boolean isDuplicatedAccount(AccountRequestDto accountDto) {
        return accountRepository.findByEmail(accountDto.getEmail()).isPresent();
    }

    public ResponseEntity<?> favorite(Long accountId, Account currentUser) {
        Optional<Account> accountOptional = accountRepository.findById(accountId);
        if (!accountOptional.isPresent()) {
            return new ResponseEntity<>(new ErrorMessage(USERNOTFOUND), HttpStatus.BAD_REQUEST);
        }
        Account favoritedAccount = accountOptional.get();
        favoritedAccount.addFavorite(currentUser);
        Account savedAccount = accountRepository.save(favoritedAccount);

        return new ResponseEntity<>(new AccountResponseDto(savedAccount), HttpStatus.OK);
    }

    public ResponseEntity<?> getFavoriteUsers(Long accountId) {
        Optional<Account> accountOptional = accountRepository.findById(accountId);
        if (!accountOptional.isPresent()) {
            return new ResponseEntity<>(new ErrorMessage(USERNOTFOUND), HttpStatus.OK);
        }
        Account account = accountOptional.get();
        List<AccountListResponseDto> accountList = account.getFavorite().stream()
                .map(AccountListResponseDto::new)
                .collect(Collectors.toList());
        return new ResponseEntity<>(accountList, HttpStatus.OK);
    }
}
