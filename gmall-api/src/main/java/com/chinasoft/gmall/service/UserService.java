package com.chinasoft.gmall.service;


import com.chinasoft.gmall.entity.UmsMember;
import com.chinasoft.gmall.entity.UmsMemberReceiveAddress;
import com.chinasoft.gmall.entity.User;

import java.util.List;

public interface UserService {
//    List<User> getAllUserser();
//
//    User getUserById(int Id);

    UmsMember login(UmsMember umsMember);

    void addUserToken(String token, String memberId);

    UmsMember  addOauthUser(UmsMember umsMember);

    UmsMember checkOauthUser(UmsMember umsCheck);

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);

    UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId);
}
