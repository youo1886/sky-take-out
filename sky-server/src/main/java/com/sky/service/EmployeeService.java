package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * @param employeeDTO
     * @param employeeDTO
     * @return
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 员工账号启用禁用
     * @status 0 禁用 1 启用
     *
     * */
    void startOrStop(Integer status, Long id);

    /**
     * 根据id查询员工
     *
     * */
    Employee getById(Long id);

    /**
     *修改员工
     * @param employeeDTO
     */
    void update(EmployeeDTO employeeDTO);
}

