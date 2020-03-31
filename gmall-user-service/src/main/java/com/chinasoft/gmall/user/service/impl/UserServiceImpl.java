package com.chinasoft.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.chinasoft.gmall.entity.UmsMember;
import com.chinasoft.gmall.entity.UmsMemberReceiveAddress;
import com.chinasoft.gmall.entity.User;
import com.chinasoft.gmall.service.UserService;
import com.chinasoft.gmall.user.mapper.UserAddressMapper;
import com.chinasoft.gmall.user.mapper.UserMapper;
import com.chinasoft.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;


import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    UserAddressMapper userAddressMapper;

//    @Override
//    public List<User> getAllUserser() {
//        List<User> userList =  userMapper.selectAll();
//        return userList;
//    }
//
//    @Override
//    public User getUserById(int id) {
////        Example e = new Example(User.class);
////        e.createCriteria().andEqualTo("uid",id);
////        User u = userMapper.selectByExample(e);
////        User u = new User();
////        u.setUid(id);
////        Example example = new Example(User.class);
////        example.createCriteria().andEqualTo("","")
////        User user  =  userMapper.selectByExample(e);
//        User u = userMapper.selectUserById(id);
//        return u;
//    }

    @Override
    public UmsMember login(UmsMember umsMember) {

        Jedis jedis = null;
        try{
            jedis = redisUtil.getJedis();
            if (jedis!=null){
                String umsMemberStr = jedis.get("user:" + umsMember.getPassword()+umsMember.getUsername() + ":info");

                if (StringUtils.isNotBlank(umsMemberStr)){
                    //密码正确
                    UmsMember umsMemberFromCache = JSON.parseObject(umsMemberStr, UmsMember.class);
                    return umsMemberFromCache;
                }
            }
            //连接redis失败，或redis中没有该值
            UmsMember umsMemberFromDb = loginFromDb(umsMember);

            if (umsMemberFromDb!=null){
                jedis.setex("user:"+umsMember.getPassword()+umsMember.getUsername()+":info",60*60*24,JSON.toJSONString(umsMemberFromDb));
            }
            return umsMemberFromDb;

        }finally {
            jedis.close();
        }
    }

    @Override
    public void addUserToken(String token, String memberId) {
        Jedis jedis = redisUtil.getJedis();
        jedis.setex("user:"+memberId+":token",60*60*2,token);
        jedis.close();
    }

    @Override
    public UmsMember addOauthUser(UmsMember umsMember) {
        userMapper.insertSelective(umsMember);
        return umsMember;
    }

    @Override
    public UmsMember checkOauthUser(UmsMember umsCheck) {
        UmsMember umsMember = userMapper.selectOne(umsCheck);
        return umsMember;
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);
        List<UmsMemberReceiveAddress> umsMemberReceiveAddressList = userAddressMapper.select(umsMemberReceiveAddress);
        return umsMemberReceiveAddressList;
    }

    @Override
    public UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId) {

        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setId(receiveAddressId);
        UmsMemberReceiveAddress umsMemberReceiveAddress1 = userAddressMapper.selectOne(umsMemberReceiveAddress);
        return umsMemberReceiveAddress1;
    }

    private UmsMember loginFromDb(UmsMember umsMember) {

         List<UmsMember> umsMembers = userMapper.select(umsMember);

         if (umsMembers!=null){
             return umsMembers.get(0);
         }
        return null;
    }


}
