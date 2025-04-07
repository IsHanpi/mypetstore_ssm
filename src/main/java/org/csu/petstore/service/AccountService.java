package org.csu.petstore.service;

import org.csu.petstore.entity.Account;
import org.csu.petstore.entity.Profile;
import org.csu.petstore.entity.SignOn;
import org.csu.petstore.vo.AccountVO;

public interface AccountService {
    AccountVO getAccount(String username);
    SignOn getAccount(String username, String password);
    Account getAccount(AccountVO accountVO);
    void insertAccount(AccountVO accountVO);
    Account getAccountByUsernameAndPassword(String username, String password);
    void updateAccount(AccountVO accountVO);
    void insertProfile(Profile profile);
    void updateProfile(Profile profile);
    void insertSignOn(String username, String password);
    void updateSignOn(String username, String password);
    void deleteSignOn(String username);
    boolean hadAccount(String username);
}
