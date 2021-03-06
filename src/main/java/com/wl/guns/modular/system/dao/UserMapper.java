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
package com.wl.guns.modular.system.dao;

import cn.stylefeng.roses.core.datascope.DataScope;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.wl.guns.modular.system.model.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 管理员表 Mapper 接口
 * </p>
 *
 * @author 王柳
 * @since 2017-07-11
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 修改用户状态
     */
    int setStatus(@Param("userId") Integer userId, @Param("status") int status);

    /**
     * 修改密码
     */
    int changePwd(@Param("userId") Integer userId, @Param("pwd") String pwd);

    /**
     * 根据条件查询用户列表
     */
    List<Map<String, Object>> selectUsers(User user);

    /**
     * 设置用户的角色
     */
    int setRoles(@Param("userId") Integer userId, @Param("roleIds") String roleIds);

    /**
     * 通过账号获取用户
     */
    User getByAccount(@Param("account") String account);

    /**
     * @description 根据条件查询user数量
     * @author 王柳
     * @date 2019/11/22 11:42
     * @params [name, beginTime, endTime]
     */
    Integer selectListCount(@Param("name") String name, @Param("beginTime") String beginTime, @Param("endTime") String endTime);

    /**
     * @description 根据条件查询要导出的user信息
     * @author 王柳
     * @date 2019/11/22 11:42
     * @params [page, name, beginTime, endTime]
     */
    List<Map<String, Object>> selectPoiExport(Pagination page, @Param("name") String name, @Param("beginTime") String beginTime, @Param("endTime") String endTime);

    List<User> selectUserList(User user);
}
