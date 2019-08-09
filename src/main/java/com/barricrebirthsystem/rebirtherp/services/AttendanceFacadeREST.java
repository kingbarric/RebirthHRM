/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.barricrebirthsystem.rebirtherp.services;

import com.barricrebirthsystem.rebirtherp.entities.Attendance;
import com.barricrebirthsystem.rebirtherp.entities.Employee;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Barima
 */
@Stateless
@Path("attendance")
public class AttendanceFacadeREST extends AbstractFacade<Attendance> {

    @PersistenceContext(unitName = "com.barricrebirthsystem_RebirthERP_war_1.0-SNAPSHOTPU")
    private EntityManager em;
    private static final String CLOCKEDIN="CLOCKEDIN";
    private static final String CLOCKEDOUT="CLOCKEDOUT";
    private static final String NOTCLOCKEDIN="NOTCLOCKEDIN";

    public AttendanceFacadeREST() {
        super(Attendance.class);
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response clockIn(Attendance att) {
           Map map = new HashMap();
        if (isClockbyEmp(att.getEmployeeId().getId()).equals(NOTCLOCKEDIN)) {
        
            try {
                
               
                if (isClockbyEmp(att.getEmployeeId().getId()).equals(NOTCLOCKEDIN)) {
                    att.setAttendanceDate(new Date());
                    att.setClockIntime(new Date());
                    this.em.persist(att);
                    map.put("code", 1);
                    map.put("status", "you have signed in for the day!");
                    return Response.ok(map).build();
                } else {
                    map.put("code", 2);
                    map.put("status", "Attendance already added for this employee");
                    return Response.ok(map).build();
                }
            } catch (Exception e) {
                map.put("code", 3);
                    map.put("status", "Error occured while signing you in!");
                    return Response.ok(map).build();
            }
    }else{
            System.err.println(isClockbyEmp(att.getEmployeeId().getId()));
           map.put("code", 3);
                    map.put("status", "Could not sign you in!");
                    return Response.ok(map).build();
        }
    
    }

    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response clockOut(Attendance entity) {
        Map map = new HashMap();
        if (isClockbyEmp(entity.getEmployeeId().getId()).equals(CLOCKEDIN)) {
            try {

                Attendance atten = em.createNamedQuery("Attendance.findEmpAndDate", Attendance.class)
                        .setParameter("attendanceDate", new Date())
                        .setParameter("empid", entity.getEmployeeId().getId())
                        .getSingleResult();
                // att.setAttendanceDate(new Date());
                atten.setClockoutTime(new Date());
                atten.setReason(entity.getReason());
                super.edit(atten);
              map.put("code", 1);
            map.put("status", "you have signed out for the day!");
            return Response.ok(map).build();
            } catch (Exception e) {
                 map.put("code", -1);
            map.put("status", "Error occured while signing out!");
            return Response.ok(map).build();
            }
        }

     map.put("code", -2);
            map.put("status", "Could not sign you out, contact admin!");
            return Response.ok(map).build();

    }

   
    public String isClockbyEmp(Integer empid) {
        String status = "";
        try {
            Date myDate = new Date();
            

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

            // Format the date to Strings
            String mdy = df.format(myDate);
            System.out.println(mdy);
            Attendance atten = em.createNamedQuery("Attendance.findEmpAndDate", Attendance.class)
                    .setParameter("attendanceDate", myDate)
                    .setParameter("empid", empid)
                    .getSingleResult();
            if(atten != null){
            if(atten.getClockoutTime() ==null){
            status = CLOCKEDIN;
            }else{
            status =CLOCKEDOUT;
            }
            }else{
            status=NOTCLOCKEDIN;
            }
        } catch (Exception e) {
            status =NOTCLOCKEDIN;
        }
        return status;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("isclocked/{empid}")
    public Response isClocked(@PathParam("empid") Integer empid) {
        Map m = new HashMap();
      
             m.put("clockStatus", isClockbyEmp(empid));
        
        
        return Response.ok(m).build();
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Integer id) {
        super.remove(super.find(id));
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Attendance find(@PathParam("id") Integer id) {
        return super.find(id);
    }

    @GET
    @Override
    @Produces({MediaType.APPLICATION_JSON})
    public List<Attendance> findAll() {
        return em.createNamedQuery("Attendance.findAll").getResultList();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Attendance> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("emp/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<Attendance> findByEmp(@PathParam("id") Integer id) {
        return em.createNamedQuery("Attendance.findByEmployeeId", Attendance.class)
                .setParameter("employeeId", new Employee(id))
                .getResultList();
    }

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST() {
        return String.valueOf(super.count());
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @POST
    @Path("saveattendance")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    public Response saveAttendance(Attendance att) {
        System.out.println("Attendance: " + att);
      
        Map map = new HashMap();
        if (isClockbyEmp(att.getEmployeeId().getId()).equals(NOTCLOCKEDIN)) {
            this.em.persist(att);
            map.put("code", 1);
            map.put("status", "you have signed in for the day!");
            return Response.ok(map).build();
        } else {
            map.put("code", 2);
            map.put("status", "Attendance already added for this employee");
            return Response.ok(map).build();
        }

    }

}
