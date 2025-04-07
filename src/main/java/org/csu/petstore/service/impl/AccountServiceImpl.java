package org.csu.petstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.csu.petstore.entity.Account;
import org.csu.petstore.entity.BannerData;
import org.csu.petstore.entity.Profile;
import org.csu.petstore.entity.SignOn;
import org.csu.petstore.persistence.AccountMapper;
import org.csu.petstore.persistence.BannerDataMapper;
import org.csu.petstore.persistence.ProfileMapper;
import org.csu.petstore.persistence.SignOnMapper;
import org.csu.petstore.service.AccountService;
import org.csu.petstore.vo.AccountVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service("accountService")
public class AccountServiceImpl implements AccountService {
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private ProfileMapper profileMapper;
    @Autowired
    private SignOnMapper signOnMapper;
    @Autowired
    private BannerDataMapper bannerDataMapper;
    @Override
    public AccountVO getAccount(String username){
        AccountVO accountVO = new AccountVO();
        Account account = accountMapper.selectById(username);
        SignOn signOn = signOnMapper.selectById(username);
        Profile profile = profileMapper.selectById(username);
        BannerData bannerData = bannerDataMapper.selectById(profile.getFavcategory());

        accountVO.setAddress1(account.getAddress1());
        accountVO.setAddress2(account.getAddress2());
        accountVO.setCity(account.getCity());
        accountVO.setCountry(account.getCountry());
        accountVO.setEmail(account.getEmail());
        accountVO.setZip(account.getZip());
        accountVO.setPhone(account.getPhone());
        accountVO.setState(account.getState());
        accountVO.setStatus(account.getStatus());
        accountVO.setFirstName(account.getFirstName());
        accountVO.setLastName(account.getLastName());
        accountVO.setUsername(username);
        accountVO.setPassword(signOn.getPassword());

        String banner = bannerData.getBannername();
        banner = banner.substring(0,12) + "../" + banner.substring(12);
        accountVO.setBannerName(banner);
        accountVO.setBannerOption(profile.getBanneropt() == 1);
        accountVO.setListOption(profile.getMylistopt() == 1);
        accountVO.setFavouriteCategoryId(profile.getFavcategory());
        accountVO.setLanguagePreference(profile.getLangpref());
        return accountVO;
    }

    @Override
    public SignOn getAccount(String userName, String password) {
        QueryWrapper<SignOn> queryWrapper = new QueryWrapper<SignOn>();
        queryWrapper.eq("username", userName);
        queryWrapper.eq("password", password);
        return signOnMapper.selectOne(queryWrapper);
    }

    @Override
    public Account getAccount(AccountVO accountVO) {
        Account account = new Account();
        account.setUsername(accountVO.getUsername());
        account.setEmail(accountVO.getEmail());
        account.setFirstName(accountVO.getFirstName());
        account.setLastName(accountVO.getLastName());
        account.setPhone(accountVO.getPhone());
        account.setState(accountVO.getState());
        account.setStatus(accountVO.getStatus());
        account.setAddress1(accountVO.getAddress1());
        account.setAddress2(accountVO.getAddress2());
        account.setCity(accountVO.getCity());
        account.setCountry(accountVO.getCountry());
        account.setZip(accountVO.getZip());
        return account;
    }

    @Override
    public void insertAccount(AccountVO accountVO) {
        Account account = new Account();
        Profile profile = new Profile();
        if(accountVO.isListOption())
            profile.setMylistopt(1);
        else
            profile.setMylistopt(0);
        if(accountVO.isBannerOption())
            profile.setBanneropt(1);
        else
            profile.setBanneropt(0);
        profile.setLangpref(accountVO.getLanguagePreference());
        profile.setFavcategory(accountVO.getFavouriteCategoryId());
        profile.setUserid(accountVO.getUsername());
        accountMapper.insert(account);
        insertProfile(profile);
        insertSignOn(account.getUsername(), accountVO.getPassword());
    }

    @Override
    public Account getAccountByUsernameAndPassword(String username, String password) {
        QueryWrapper<Account> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("password", password);
        return accountMapper.selectOne(queryWrapper);
    }

    @Override
    public void updateAccount(AccountVO accountVO) {
        Account account = getAccount(accountVO);
        Profile profile = new Profile();
        if(accountVO.isListOption())
            profile.setMylistopt(1);
        else
            profile.setMylistopt(0);
        if(accountVO.isBannerOption())
            profile.setBanneropt(1);
        else
            profile.setBanneropt(0);
        profile.setLangpref(accountVO.getLanguagePreference());
        profile.setFavcategory(accountVO.getFavouriteCategoryId());
        profile.setUserid(accountVO.getUsername());
        accountMapper.updateById(account);
        updateProfile(profile);
        updateSignOn(account.getUsername(), accountVO.getPassword());
    }

    @Override
    public void insertProfile(Profile profile) {
        profileMapper.insert(profile);
    }

    @Override
    public void updateProfile(Profile profile) {
        profileMapper.updateById(profile);
    }

    @Override
    public void insertSignOn(String username, String password) {
        SignOn signOn = new SignOn();
        signOn.setUsername(username);
        signOn.setPassword(password);
        signOnMapper.updateById(signOn);
    }

    @Override
    public void updateSignOn(String username, String password) {
        SignOn signOn = new SignOn();
        signOn.setUsername(username);
        signOn.setPassword(password);
        signOnMapper.updateById(signOn);
    }

    @Override
    public void deleteSignOn(String username) {
        signOnMapper.deleteById(username);
    }

    @Override
    public boolean hadAccount(String username) {
        QueryWrapper<Account> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userid",username);
        return accountMapper.exists(queryWrapper);
    }
}
