/**
 * Copyright 2018-2020 stylefeng & fengshuonan (https://gitee.com/stylefeng)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl.guns.modular.system.service;

import cn.stylefeng.roses.core.reqres.response.ResponseData;
import com.wl.guns.modular.system.model.User;
import cn.stylefeng.roses.core.datascope.DataScope;
import com.baomidou.mybatisplus.service.IService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 管理员表 服务类
 * </p>
 *
 * @author 王柳123
 * @since 2018-02-22
 */
public interface IUserService extends IService<User> {

    /**
     * 修改用户状态
     */
    int setStatus(Integer userId, int status);

    /**
     * 修改密码
     */
    int changePwd(Integer userId, String pwd);

    /**
     * 根据条件查询用户列表
     */
    List<Map<String, Object>> selectUsers(User user);

    /**
     * 设置用户的角色
     */
    int setRoles(Integer userId, String roleIds);

    /**
     * 通过账号获取用户
     */
    User getByAccount(String account);

    /**
     * @description  POI导出
     * @author 王柳
     * @date 2019/11/22 12:56
     * @params [request, response]
     */
    void exportPoi(HttpServletRequest request, HttpServletResponse response) throws Exception;

    /**
     * @description  POI导入，写入数据
     * @author 王柳
     * @date 2019/11/22 12:56
     * @params [request, response]
     * @return
     */
    List<User> writeExelData(InputStream is);

    List<User> selectUserList(User user);

    ResponseData importUser(List<User> userList, boolean updateSupport);
}
