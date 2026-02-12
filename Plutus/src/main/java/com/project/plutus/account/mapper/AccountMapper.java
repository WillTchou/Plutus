package com.project.plutus.account.mapper;

import com.project.plutus.account.model.Account;
import com.project.plutus.account.model.AccountDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountDTO toAccountDTO(Account account);
}
