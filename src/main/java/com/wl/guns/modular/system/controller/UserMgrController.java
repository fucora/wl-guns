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
package com.wl.guns.modular.system.controller;

import cn.stylefeng.roses.core.base.controller.BaseController;
import cn.stylefeng.roses.core.datascope.DataScope;
import cn.stylefeng.roses.core.reqres.response.ResponseData;
import cn.stylefeng.roses.core.util.ToolUtil;
import cn.stylefeng.roses.kernel.model.exception.ServiceException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.wl.guns.config.properties.GunsProperties;
import com.wl.guns.core.common.annotion.BussinessLog;
import com.wl.guns.core.common.annotion.Permission;
import com.wl.guns.core.common.constant.Const;
import com.wl.guns.core.common.constant.dictmap.UserDict;
import com.wl.guns.core.common.constant.factory.ConstantFactory;
import com.wl.guns.core.common.constant.state.ManagerStatus;
import com.wl.guns.core.common.exception.BizExceptionEnum;
import com.wl.guns.core.log.LogObjectHolder;
import com.wl.guns.core.shiro.ShiroKit;
import com.wl.guns.core.shiro.ShiroUser;
import com.wl.guns.core.util.annotationexcel.ExcelUtil;
import com.wl.guns.core.util.poiexcel.ExcelConstant;
import com.wl.guns.modular.system.factory.UserFactory;
import com.wl.guns.modular.system.model.User;
import com.wl.guns.modular.system.service.IUserService;
import com.wl.guns.modular.system.transfer.UserDto;
import com.wl.guns.modular.system.warpper.UserWarpper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.NoPermissionException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 系统管理员控制器
 *
 * @author 王柳
 * @Date 2017年1月11日 下午1:08:17
 */
@Controller
@RequestMapping("/mgr")
@Slf4j
public class UserMgrController extends BaseController {

    private static String PREFIX = "/system/user/";

    @Autowired
    private GunsProperties gunsProperties;

    @Autowired
    private IUserService userService;

    /**
     * 跳转到查看管理员列表的页面
     */
    @RequestMapping("")
    public String index() {
        return PREFIX + "user.html";
    }

    /**
     * 跳转到查看管理员列表的页面
     */
    @RequestMapping("/user_add")
    public String addView() {
        return PREFIX + "user_add.html";
    }

    /**
     * 跳转到角色分配页面
     */
    //@RequiresPermissions("/mgr/role_assign")  //利用shiro自带的权限检查
    @Permission
    @RequestMapping("/role_assign/{userId}")
    public String roleAssign(@PathVariable Integer userId, Model model) {
        if (ToolUtil.isEmpty(userId)) {
            throw new ServiceException(BizExceptionEnum.REQUEST_NULL);
        }
        User user = this.userService.selectOne(new EntityWrapper<User>().eq("id", userId));
        model.addAttribute("userId", userId);
        model.addAttribute("userAccount", user.getAccount());
        return PREFIX + "user_roleassign.html";
    }

    /**
     * 跳转到编辑管理员页面
     */
    @Permission
    @RequestMapping("/user_edit/{userId}")
    public String userEdit(@PathVariable Integer userId, Model model) {
        if (ToolUtil.isEmpty(userId)) {
            throw new ServiceException(BizExceptionEnum.REQUEST_NULL);
        }
        assertAuth(userId);
        User user = this.userService.selectById(userId);
        model.addAttribute(user);
        model.addAttribute("roleName", ConstantFactory.me().getRoleName(user.getRoleid()));
        model.addAttribute("deptName", ConstantFactory.me().getDeptName(user.getDeptid()));
        LogObjectHolder.me().set(user);
        return PREFIX + "user_edit.html";
    }

    /**
     * 跳转到查看用户详情页面
     */
    @RequestMapping("/user_info")
    public String userInfo(Model model) {
        Integer userId = ShiroKit.getUser().getId();
        if (ToolUtil.isEmpty(userId)) {
            throw new ServiceException(BizExceptionEnum.REQUEST_NULL);
        }
        User user = this.userService.selectById(userId);
        model.addAttribute(user);
        model.addAttribute("roleName", ConstantFactory.me().getRoleName(user.getRoleid()));
        model.addAttribute("deptName", ConstantFactory.me().getDeptName(user.getDeptid()));
        LogObjectHolder.me().set(user);
        return PREFIX + "user_view.html";
    }

    /**
     * 跳转到修改密码界面
     */
    @RequestMapping("/user_chpwd")
    public String chPwd() {
        return PREFIX + "user_chpwd.html";
    }

    /**
     * 修改当前用户的密码
     */
    @RequestMapping("/changePwd")
    @ResponseBody
    public Object changePwd(@RequestParam String oldPwd, @RequestParam String newPwd, @RequestParam String rePwd) {
        if (!newPwd.equals(rePwd)) {
            throw new ServiceException(BizExceptionEnum.TWO_PWD_NOT_MATCH);
        }
        Integer userId = ShiroKit.getUser().getId();
        User user = userService.selectById(userId);
        String oldMd5 = ShiroKit.md5(oldPwd, user.getSalt());
        if (user.getPassword().equals(oldMd5)) {
            String newMd5 = ShiroKit.md5(newPwd, user.getSalt());
            user.setPassword(newMd5);
            user.updateById();
            return SUCCESS_TIP;
        } else {
            throw new ServiceException(BizExceptionEnum.OLD_PWD_NOT_RIGHT);
        }
    }

    /**
     * 查询管理员列表
     */
    @RequestMapping("/list")
    @Permission
    @ResponseBody
    public Object list(User user) {
        List<Map<String, Object>> users = userService.selectUsers(user);
        return new UserWarpper(users).wrap();
    }

    /**
     * 添加管理员
     */
    @RequestMapping("/add")
    @BussinessLog(value = "添加管理员", key = "account", dict = UserDict.class)
    @Permission(Const.ADMIN_NAME)
    @ResponseBody
    public ResponseData add(@Valid UserDto user, BindingResult result) {
        if (result.hasErrors()) {
            throw new ServiceException(BizExceptionEnum.REQUEST_NULL);
        }

        // 判断账号是否重复
        User theUser = userService.getByAccount(user.getAccount());
        if (theUser != null) {
            throw new ServiceException(BizExceptionEnum.USER_ALREADY_REG);
        }

        // 完善账号信息
        user.setSalt(ShiroKit.getRandomSalt(5));
        user.setPassword(ShiroKit.md5(user.getPassword(), user.getSalt()));
        user.setStatus(ManagerStatus.OK.getCode());
        user.setCreatetime(new Date());

        this.userService.insert(UserFactory.createUser(user));
        return SUCCESS_TIP;
    }

    /**
     * 修改管理员
     *
     * @throws NoPermissionException
     */
    @RequestMapping("/edit")
    @BussinessLog(value = "修改管理员", key = "account", dict = UserDict.class)
    @ResponseBody
    public ResponseData edit(@Valid UserDto user, BindingResult result) throws NoPermissionException {
        if (result.hasErrors()) {
            throw new ServiceException(BizExceptionEnum.REQUEST_NULL);
        }

        User oldUser = userService.selectById(user.getId());

        if (ShiroKit.hasRole(Const.ADMIN_NAME)) {
            this.userService.updateById(UserFactory.editUser(user, oldUser));
            return SUCCESS_TIP;
        } else {
            assertAuth(user.getId());
            ShiroUser shiroUser = ShiroKit.getUser();
            if (shiroUser.getId().equals(user.getId())) {
                this.userService.updateById(UserFactory.editUser(user, oldUser));
                return SUCCESS_TIP;
            } else {
                throw new ServiceException(BizExceptionEnum.NO_PERMITION);
            }
        }
    }

    /**
     * 删除管理员（逻辑删除）
     */
    @RequestMapping("/delete")
    @BussinessLog(value = "删除管理员", key = "userId", dict = UserDict.class)
    @Permission
    @ResponseBody
    public ResponseData delete(@RequestParam Integer userId) {
        if (ToolUtil.isEmpty(userId)) {
            throw new ServiceException(BizExceptionEnum.REQUEST_NULL);
        }
        //不能删除超级管理员
        if (userId.equals(Const.ADMIN_ID)) {
            throw new ServiceException(BizExceptionEnum.CANT_DELETE_ADMIN);
        }
        assertAuth(userId);
        this.userService.setStatus(userId, ManagerStatus.DELETED.getCode());
        return SUCCESS_TIP;
    }

    /**
     * 查看管理员详情
     */
    @RequestMapping("/view/{userId}")
    @ResponseBody
    public User view(@PathVariable Integer userId) {
        if (ToolUtil.isEmpty(userId)) {
            throw new ServiceException(BizExceptionEnum.REQUEST_NULL);
        }
        assertAuth(userId);
        return this.userService.selectById(userId);
    }

    /**
     * 重置管理员的密码
     */
    @RequestMapping("/reset")
    @BussinessLog(value = "重置管理员密码", key = "userId", dict = UserDict.class)
    @Permission(Const.ADMIN_NAME)
    @ResponseBody
    public ResponseData reset(@RequestParam Integer userId) {
        if (ToolUtil.isEmpty(userId)) {
            throw new ServiceException(BizExceptionEnum.REQUEST_NULL);
        }
        assertAuth(userId);
        User user = this.userService.selectById(userId);
        user.setSalt(ShiroKit.getRandomSalt(5));
        user.setPassword(ShiroKit.md5(Const.DEFAULT_PWD, user.getSalt()));
        this.userService.updateById(user);
        return SUCCESS_TIP;
    }

    /**
     * 冻结用户
     */
    @RequestMapping("/freeze")
    @BussinessLog(value = "冻结用户", key = "userId", dict = UserDict.class)
    @Permission(Const.ADMIN_NAME)
    @ResponseBody
    public ResponseData freeze(@RequestParam Integer userId) {
        if (ToolUtil.isEmpty(userId)) {
            throw new ServiceException(BizExceptionEnum.REQUEST_NULL);
        }
        //不能冻结超级管理员
        if (userId.equals(Const.ADMIN_ID)) {
            throw new ServiceException(BizExceptionEnum.CANT_FREEZE_ADMIN);
        }
        assertAuth(userId);
        this.userService.setStatus(userId, ManagerStatus.FREEZED.getCode());
        return SUCCESS_TIP;
    }

    /**
     * 解除冻结用户
     */
    @RequestMapping("/unfreeze")
    @BussinessLog(value = "解除冻结用户", key = "userId", dict = UserDict.class)
    @Permission(Const.ADMIN_NAME)
    @ResponseBody
    public ResponseData unfreeze(@RequestParam Integer userId) {
        if (ToolUtil.isEmpty(userId)) {
            throw new ServiceException(BizExceptionEnum.REQUEST_NULL);
        }
        assertAuth(userId);
        this.userService.setStatus(userId, ManagerStatus.OK.getCode());
        return SUCCESS_TIP;
    }

    /**
     * 分配角色
     */
    @RequestMapping("/setRole")
    @BussinessLog(value = "分配角色", key = "userId,roleIds", dict = UserDict.class)
    @Permission(Const.ADMIN_NAME)
    @ResponseBody
    public ResponseData setRole(@RequestParam("userId") Integer userId, @RequestParam("roleIds") String roleIds) {
        if (ToolUtil.isOneEmpty(userId, roleIds)) {
            throw new ServiceException(BizExceptionEnum.REQUEST_NULL);
        }
        //不能修改超级管理员
        if (userId.equals(Const.ADMIN_ID)) {
            throw new ServiceException(BizExceptionEnum.CANT_CHANGE_ADMIN);
        }
        assertAuth(userId);
        this.userService.setRoles(userId, roleIds);
        return SUCCESS_TIP;
    }

    /**
     * 上传图片
     */
    @RequestMapping(method = RequestMethod.POST, path = "/upload")
    @ResponseBody
    public String upload(@RequestPart("file") MultipartFile picture) {

        String pictureName = UUID.randomUUID().toString() + "." + ToolUtil.getFileSuffix(picture.getOriginalFilename());
        try {
            String fileSavePath = gunsProperties.getFileUploadPath();
            picture.transferTo(new File(fileSavePath + pictureName));
        } catch (Exception e) {
            throw new ServiceException(BizExceptionEnum.UPLOAD_ERROR);
        }
        return pictureName;
    }

    /**
     * 判断当前登录的用户是否有操作这个用户的权限
     */
    private void assertAuth(Integer userId) {
        if (ShiroKit.isAdmin()) {
            return;
        }
        List<Integer> deptDataScope = ShiroKit.getDeptDataScope();
        User user = this.userService.selectById(userId);
        Integer deptid = user.getDeptid();
        if (deptDataScope.contains(deptid)) {
            return;
        } else {
            throw new ServiceException(BizExceptionEnum.NO_PERMITION);
        }

    }

    /**
     * @description POI导出
     * @author 王柳
     * @date 2019/11/22 9:41
     * @params [request, response]
     */
    @RequestMapping("/exportPoi")
    public void exportPoi(HttpServletRequest request, HttpServletResponse response) {
        getSession().setAttribute(ExcelConstant.EXPORT_FLAG, "true");

        try {
            userService.exportPoi(request, response);
        } catch (Exception e) {
            log.error("POI导出Excel异常{}", e.getMessage());
        }
    }

    /**
     * @description 判断是否POI导出excel是否完成，用于loading的显示和隐藏
     * @author 王柳
     * @date 2019/11/22 9:45
     * @params []
     */
    @RequestMapping("/isPoiExport")
    @ResponseBody
    public Object isExport() {
        JSONObject jsonObject = new JSONObject();
        if (getSession().getAttribute(ExcelConstant.EXPORT_FLAG) == null) {
            jsonObject.put(ExcelConstant.RESULT_CODE, 0);
        } else {
            jsonObject.put(ExcelConstant.RESULT_CODE, -1);
        }
        return jsonObject;
    }

    /**
     * 跳转到POI导入页面
     */
    @RequestMapping("/importPoiExcel")
    public String importPoiExcel() {
        return PREFIX + "user_import_poi_excel.html";
    }

    /**
     * @description POI导入
     * @author 王柳
     * @date 2019/11/22 9:51
     * @params [file]
     */
    @RequestMapping("/importPoi")
    @ResponseBody
    public Object importPoi(@RequestParam("file") MultipartFile file) {
        //上传标志
        String flag = ExcelConstant.IMPORT_NULL;

        // 判断文件是否为空
        if (!file.isEmpty()) {
            try {
                //获取输入流
                InputStream is = file.getInputStream();
                List<User> userList = userService.writeExelData(is);
                if (userList.size() == 0) {
                    flag = ExcelConstant.IMPORT_NULL;
                } else {
                    // 批量导入
                    Boolean isSuccess = userService.insertBatch(userList);
                    if (isSuccess) {
                        flag = ExcelConstant.IMPORT_SUCCESS;
                    } else {
                        flag = ExcelConstant.IMPORT_ERROR;
                    }
                }
            } catch (Exception e) {
                //上传出错
                flag = ExcelConstant.IMPORT_ERROR;
                log.error("POI导入Excel异常{}", e.getMessage());
            }
        }
        return flag;
    }

    /**
     * @description 注解导出
     * @author 王柳
     * @date 2019/11/25 10:22
     * @params [user]
     */
    @RequestMapping("/exportAnnotation")
    @ResponseBody
    public Object exportAnnotation(@RequestBody User user) {
        List<User> list = userService.selectUserList(user);
        ExcelUtil<User> util = new ExcelUtil<User>(User.class);
        return util.exportExcel(list, "用户数据");
    }

    /**
     * @description 注解下载模板
     * @author 王柳
     * @date 2019/11/25 11:36
     * @params []
     */
    @RequestMapping("/downloadTemplateAnnotation")
    @ResponseBody
    public Object downloadTemplateAnnotation() {
        ExcelUtil<User> util = new ExcelUtil<User>(User.class);
        return util.importTemplateExcel("用户数据");
    }

    /**
     * 跳转到注解导入页面
     */
    @RequestMapping("/importAnnotationExcel")
    public String importAnnotationExcel() {
        return PREFIX + "user_import_annotation_excel.html";
    }

    /**
     * @description 注解导入
     * @author 王柳
     * @date 2019/11/25 11:56
     * @params [file, updateSupport]
     */
    @RequestMapping("/importAnnotation")
    @ResponseBody
    public Object importAnnotation(MultipartFile file, boolean updateSupport) throws Exception {
        ExcelUtil<User> util = new ExcelUtil<User>(User.class);
        List<User> userList = util.importExcel(file.getInputStream());
        ResponseData responseData = userService.importUser(userList, updateSupport);
        if (ResponseData.DEFAULT_SUCCESS_CODE.equals(responseData.getCode())) {
            Map map = (Map) responseData.getData();
            List<User> newUserList = (List<User>) map.get("new");
            List<User> oldUserList = (List<User>) map.get("old");
            if (newUserList.size() != 0) {
                userService.insertBatch(newUserList);
            }
            if (oldUserList.size() != 0) {
                userService.updateBatchById(oldUserList);
            }
        }
        return responseData;
    }
}
