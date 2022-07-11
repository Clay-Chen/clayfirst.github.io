package com.clay.uesrcenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.clay.uesrcenter.common.BaseResponse;
import com.clay.uesrcenter.common.ErrorCode;
import com.clay.uesrcenter.common.ResultUtils;
import com.clay.uesrcenter.exception.BusinessException;
import com.clay.uesrcenter.model.domain.User;
import com.clay.uesrcenter.model.request.UserLoginRequest;
import com.clay.uesrcenter.model.request.UserRegisterRequest;
import com.clay.uesrcenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.jws.soap.SOAPBinding;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.clay.uesrcenter.contant.UserConstant.AOMIN_POLE;
import static com.clay.uesrcenter.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author clay
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService service;
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if (userRegisterRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAllBlank(userAccount,userPassword,checkPassword,planetCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long result =   service.userRegister(userAccount, userPassword, checkPassword,planetCode);
          //return  new BaseResponse<>(0,result,"ok");
        return ResultUtils.success(result);
    }
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if (userLoginRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if (StringUtils.isAllBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user= service.doLogin(userAccount, userPassword,request);
        return ResultUtils.success(user);

    }
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request){
        if (request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }


        int i= service.userLogout(request);
        return ResultUtils.success(i);

    }
   
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) attribute;
        if (currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        //todo 校验用户是否合法
        User user = service.getById(userId);
        User safetyUser = service.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username,HttpServletRequest request ){
        if (!isAdmin(request)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)){
            userQueryWrapper.like("username",username);
        }
        List<User> userList = service.list(userQueryWrapper);
        List<User> collect = userList.stream().map(user -> {
            user.setUserpassword(null);
            return service.getSafetyUser(user);
        }).collect(Collectors.toList());
        return ResultUtils.success(collect);
    }
    @PostMapping ("/delete")
    public BaseResponse<Boolean> deleteUsers(@RequestBody long id,HttpServletRequest request){
        if (!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = service.removeById(id);
         return ResultUtils.success(b);
    }
private  boolean isAdmin(HttpServletRequest request){
    //仅管理员可查询
    Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
    User user = (User) userObj;
    if (user == null || user.getUserRole() !=AOMIN_POLE){
        return  false;
    }
    return true;
}
}
