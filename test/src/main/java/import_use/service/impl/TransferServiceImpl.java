package import_use.service.impl;

import import_use.entity.AccountRepository;
import import_use.service.TransferService;

/**
 * @author JASONJ
 * @dateTime: 2021-05-15 14:47:03
 * @description: impl
 */
public class TransferServiceImpl implements TransferService {
    private AccountRepository accountRepository;
    public TransferServiceImpl(AccountRepository accountRepository){
        this.accountRepository = accountRepository;
    }
}
