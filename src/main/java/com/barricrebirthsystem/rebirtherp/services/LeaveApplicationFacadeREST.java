
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.barricrebirthsystem.rebirtherp.services;

import com.barricrebirthsystem.rebirtherp.entities.Employee;
import com.barricrebirthsystem.rebirtherp.entities.LeaveApplication;
import com.barricrebirthsystem.rebirtherp.entities.LeaveApprovals;
import com.barricrebirthsystem.rebirtherp.entities.LeaveCategory;
import com.barricrebirthsystem.rebirtherp.util.LeaveAppJsonObject;
import com.barricrebirthsystem.rebirtherp.util.UtilHelper;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
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
@Path("leaveapplication")
public class LeaveApplicationFacadeREST extends AbstractFacade<LeaveApplication> {

    @PersistenceContext(unitName = "com.barricrebirthsystem_RebirthERP_war_1.0-SNAPSHOTPU")
    private EntityManager em;

    public LeaveApplicationFacadeREST() {
        super(LeaveApplication.class);
    }

    @POST
    //@Override
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response createE(LeaveApplication entity) {
       
        Map m = new HashMap();
        try {

            
           long diff = entity.getEndDate().getTime() - entity.getStartDate().getTime();
            Long totalDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            System.out.println(totalDays);
            if(totalDays<=0){
            m.put("code", -3);
            m.put("message", "End Date cannot be less than Start Date, Please review your Dates!!! ");
           return Response.ok(m).build();
            }

            Double  days = betweenDaysIgnoreWeekends(entity.getStartDate(), entity.getEndDate());
            
            entity.setDurationInDays(days);
               Employee emp = entity.getEmpid();
            Double daysTaken = 0.0;
            LeaveCategory leaveCat =em.find(LeaveCategory.class, entity.getLeavecat().getId()) ;
            //Get all leave of this employee
            List<LeaveApplication> allLeave = em.createNamedQuery("LeaveApplication.findByEmp", LeaveApplication.class)
                    .setParameter("empid", emp).getResultList();
            //filter where category is this category
            for(LeaveApplication  l: allLeave){
            if(Objects.equals(leaveCat.getId(), l.getLeavecat().getId())){
                LeaveApprovals lap = em.find(LeaveApprovals.class, l.getId());
                        if("APPROVED".equals(lap.getApproval3Status())){
                        daysTaken = daysTaken + l.getDurationInDays();
                        }
            }
            }
            
            Double daysAvai = leaveCat.getDurationInDays() - daysTaken;
            System.out.println("DaysAvailable "+daysAvai);
             System.out.println("Taken "+days);
            if(entity.getDurationInDays()>daysAvai){
             m.put("code", -2);
            m.put("message", "Error: Your days remaining in this category is "+daysAvai+" ,you are requesting "+days+" days!(Excludes weekends!), please reduce number of days or contact Hr");
            }else{
            super.create(entity);
            m.put("code", 0);
            m.put("message", "Leave created! ");
            }
          
            

        } catch (Exception e) {
            e.printStackTrace();
            m.put("code", -1);
            m.put("message", "Error occured while creating leave, please contact Admin!!! "+e.getMessage()+" ERR: "+e.getLocalizedMessage());
        }

        return Response.ok(m).build();
    }

    @PUT
    //@Override
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public HashMap<String, String> editE(LeaveApplication entity) {
        try {
            super.edit(entity);
        } catch (Exception e) {
            System.err.println("ERr" + e);
            return UtilHelper.ErrorMessage();
        }

        return UtilHelper.SuccessMessage();
    }

    @DELETE
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response remove(@PathParam("id") Integer id) {
        LeaveApplication ex = super.find(id);
        Map map = new HashMap();
        LeaveApprovals app = em.createNamedQuery("LeaveApprovals.findByLeaveId", LeaveApprovals.class)
                .setParameter("leaveId", ex).getSingleResult();
       
        if (!"PENDING".equals(app.getApproval3Status())) {
            //Already approve by manager so cannot delete
            map.put("code", 1);
            map.put("status", "Can not delete: Already approved by Director");
            return Response.ok(map).build();
        } else {
            map.put("code", 1);
            map.put("status", "Deleted");
            super.remove(super.find(id));
            return Response.ok(map).build();
        }

    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public LeaveApplication find(@PathParam("id") Integer id) {
        return super.find(id);
    }

    @GET
    @Override
    @Produces({MediaType.APPLICATION_JSON})
    public List<LeaveApplication> findAll() {
        return em.createNamedQuery("LeaveApplication.findAll").getResultList();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<LeaveApplication> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST() {
        return String.valueOf(super.count());
    }

    @GET
    @Path("emp/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<LeaveApplication> findByEmp(@PathParam("id") Integer id) {
        return em.createNamedQuery("LeaveApplication.findByEmp", LeaveApplication.class)
                .setParameter("empid", new Employee(id))
                .getResultList();
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @GET
    @Path("leaveid/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public LeaveAppJsonObject findLeave(@PathParam("id") Integer id) {
        try {
            System.out.println("LEAVEID: "+id);
            LeaveApplication la = em.find(LeaveApplication.class, id);
            
            LeaveApprovals lv = (LeaveApprovals) em.createNamedQuery("LeaveApprovals.findByLeaveId")
                    .setParameter("leaveId", la)
                    .getSingleResult();
            LeaveCategory leaveCat = la.getLeavecat();
            Employee emp = la.getEmpid();
            Double daysTaken = 0.0;
            //Get all leave of this employee
            List<LeaveApplication> allLeave = em.createNamedQuery("LeaveApplication.findByEmp", LeaveApplication.class)
                    .setParameter("empid", emp).getResultList();
            //filter where category is this category
            for(LeaveApplication  l: allLeave){
            if(Objects.equals(leaveCat.getId(), l.getLeavecat().getId())){
                LeaveApprovals lap = em.find(LeaveApprovals.class, l.getId());
                        if("APPROVED".equals(lap.getApproval3Status())){
                        daysTaken = daysTaken + l.getDurationInDays();
                        }
            }
            }
           

//            String sql = "SELECT SUM(la.duration_in_days) as din FROM leave_approvals l  JOIN leave_application la ON(l.leave_id=la.id) "
//                    + " WHERE la.leave_cat =" + lv.getLeaveId().getLeavecat().getId() + " AND l.approval3_status ='APPROVED' group by la.leave_cat ";
//
//            
//
//            try {
//                daysTaken = (Double) em.createNativeQuery(sql).setMaxResults(1).getSingleResult();
//                System.out.println("Days taken: "+daysTaken);
//            } catch (Exception e) {
//            }
       //     int daysAllowed = lv.getLeaveId().getLeavecat().getDurationInDays();
            Double daysAvai = leaveCat.getDurationInDays() - daysTaken;
            LeaveAppJsonObject obj = new LeaveAppJsonObject();
            obj.setLeaveApp(lv);
            obj.setDaysTaken(daysTaken.intValue());
            obj.setDaysRemain(daysAvai.intValue());
            boolean flag = daysAvai > 0;
            obj.setShouldApprove(flag);
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ERROR: " + e.getMessage());
            return null;
        }
    }

    @GET
    @Path("depthead/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<LeaveApplication> findByDeptHead(@PathParam("id") Integer id) {
        
        
        String sql = "SELECT  l.*, c.name, a.approval3_status  from department d left join deptemployee de ON(d.id=de.DeptID) "
                + " JOIN leave_application l ON (l.emp_id=de.EmployeeID) join leave_approvals a on (l.id=a.leave_id)"
                + " JOIN leave_category c ON (l.leave_cat=c.id) WHERE d.department_head = " + id+" AND a.approval3_status !='PENDING' ORDER BY l.id DESC";
        return em.createNativeQuery(sql, LeaveApplication.class).getResultList();

    }

    @GET
    @Path("manager")
    @Produces({MediaType.APPLICATION_JSON})
    public List<LeaveApplication> findByManager() {
        String sql = "SELECT l.*,c.name,a.approval3_status  FROM leave_application l join leave_approvals a on (l.id=a.leave_id) JOIN leave_category c "
                + " ON (l.leave_cat=c.id) WHERE a.approval1_status='APPROVED'  AND a.approval2_status ='APPROVED' AND a.approval3_status !='PENDING' ORDER BY l.id DESC ";
        return em.createNativeQuery(sql, LeaveApplication.class).getResultList();
    }

    @GET
    @Path("hr")
    @Produces({MediaType.APPLICATION_JSON})
    public List<LeaveApplication> findByHr() {
        String sql = "SELECT l.*, a.approval3_status, c.name FROM leave_application l join leave_approvals a on (l.id=a.leave_id) JOIN leave_category c "
                + " ON (l.leave_cat=c.id) WHERE a.approval1_status='APPROVED' AND a.approval3_status !='PENDING'  ORDER BY l.id DESC ";
        return em.createNativeQuery(sql, LeaveApplication.class).getResultList();
    }
    
    
       @GET
    @Path("deptheadpending/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<LeaveApplication> findByDeptHeadPending(@PathParam("id") Integer id) {
        
        
        String sql = "SELECT  l.*, c.name, a.approval3_status  from department d left join deptemployee de ON(d.id=de.DeptID) "
                + " JOIN leave_application l ON (l.emp_id=de.EmployeeID) join leave_approvals a on (l.id=a.leave_id)"
                + " JOIN leave_category c ON (l.leave_cat=c.id) WHERE d.department_head = " + id+" AND a.approval3_status ='PENDING' ORDER BY l.id DESC";
        return em.createNativeQuery(sql, LeaveApplication.class).getResultList();

    }

    @GET
    @Path("managerpending")
    @Produces({MediaType.APPLICATION_JSON})
    public List<LeaveApplication> findByManagerPending() {
        String sql = "SELECT l.*,c.name,a.approval3_status  FROM leave_application l join leave_approvals a on (l.id=a.leave_id) JOIN leave_category c "
                + " ON (l.leave_cat=c.id) WHERE a.approval1_status='APPROVED'  AND a.approval2_status ='APPROVED' AND a.approval3_status ='PENDING' ORDER BY l.id DESC ";
        return em.createNativeQuery(sql, LeaveApplication.class).getResultList();
    }

    @GET
    @Path("hrpending")
    @Produces({MediaType.APPLICATION_JSON})
    public List<LeaveApplication> findByHrPending() {
        String sql = "SELECT l.*, a.approval3_status, c.name FROM leave_application l join leave_approvals a on (l.id=a.leave_id) JOIN leave_category c "
                + " ON (l.leave_cat=c.id) WHERE a.approval1_status='APPROVED' AND a.approval3_status ='PENDING' ORDER BY l.id DESC ";
        return em.createNativeQuery(sql, LeaveApplication.class).getResultList();
    }
    
    
    public Double betweenDaysIgnoreWeekends(Date date1, Date date2) {
    
    Calendar cal1 = Calendar.getInstance();
    Calendar cal2 = Calendar.getInstance();
    cal1.setTime(date1);
    cal2.setTime(date2);

    Double numberOfDays = 0.0;
    while (cal1.before(cal2)) {
        if ((Calendar.SATURDAY != cal1.get(Calendar.DAY_OF_WEEK))
           &&(Calendar.SUNDAY != cal1.get(Calendar.DAY_OF_WEEK))) {
            numberOfDays++;
        }
        cal1.add(Calendar.DATE,1);
    }
    return numberOfDays;
}

}
