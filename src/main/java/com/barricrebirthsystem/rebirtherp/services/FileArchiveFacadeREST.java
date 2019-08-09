/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.barricrebirthsystem.rebirtherp.services;

import com.barricrebirthsystem.rebirtherp.entities.DeptEmployee;
import com.barricrebirthsystem.rebirtherp.entities.Employee;
import com.barricrebirthsystem.rebirtherp.entities.FileArchive;
import com.barricrebirthsystem.rebirtherp.util.UtilHelper;
import com.google.gson.Gson;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
@Path("filearchive")
public class FileArchiveFacadeREST extends AbstractFacade<FileArchive> {

    @PersistenceContext(unitName = "com.barricrebirthsystem_RebirthERP_war_1.0-SNAPSHOTPU")
    private EntityManager em;

    Map map = new HashMap();

    public FileArchiveFacadeREST() {
        super(FileArchive.class);
    }

    @POST
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.APPLICATION_JSON})
    public Response uploadFile(@FormDataParam("upload") InputStream is,
            @FormDataParam("upload") FormDataContentDisposition formData,
            @FormDataParam("data") String data) {

        String fileP = Calendar.getInstance().getTimeInMillis() + formData.getFileName();

        if (this.checkFile(formData.getFileName())) {
            if (UtilHelper.saveFile(is, fileP)) {

                try {
                    String fname = formData.getFileName();
                    String ext = fname.substring(fname.lastIndexOf(".") + 1);
                    Gson json = new Gson();

                    FileArchive f = json.fromJson(data, FileArchive.class);

                    // f.setLastModifiedDate(formData.getModificationDate());
                    f.setType(ext);
                    f.setFilepath(fileP);
                    super.create(f);

                    map.put("code", 0);
                    map.put("message", "File Uploaded");
                } catch (Exception exp) {
                    System.err.println("ERROR: " + exp);
                    map.put("code", 3);
                    map.put("message", "Exception Error " + exp.getMessage());
                }

            } else {
                map.put("code", 2);
                map.put("message", "Error while uploading, please try again");
            }
        } else {
            map.put("code", 1);
            map.put("message", "Could not upload file, extention may not be surpported!");
        }

        return Response.ok(map).build();
    }

    public boolean checkFile(String filename) {

        try {
            int dot = filename.lastIndexOf(".");
            if (dot == -1) {
                return false;
            }

            String ext = filename.substring(dot + 1);
            if (ext.equalsIgnoreCase("exe") || ext.equalsIgnoreCase("dll")) {

                return false;
            }

        } catch (Exception ex) {
            return false;
        }

        return true;

    }

    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_JSON})
    public void edit(@PathParam("id") Integer id, FileArchive entity) {
        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public Response remove(@PathParam("id") Integer id) {
         Map m = new HashMap();
        try {
            FileArchive f = em.find(FileArchive.class, id);
            String filePath = UtilHelper.UPLOAD_PATH + f.getFilepath();
            File fp = new File(filePath);
            if (fp.exists()) {
                fp.delete();
            }
            em.remove(f);
           
            m.put("code", 0);
            m.put("message", "File Deleted!");
            return Response.ok(m).build();
        } catch (Exception e) {
             m.put("code", -1);
            m.put("message", "Error occured while Deleting File!");
            return Response.ok(m).build();
        }
       
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response find(@PathParam("id") Integer id) {
        System.err.println("ID: " + id);
        DeptEmployee de = null;
        List<FileArchive> att = null;
        try {
            de = (DeptEmployee) em.createNamedQuery("DeptEmployee.findByEmpid").
                    setParameter("id", new Employee(id)).getSingleResult();

            att = em.createNativeQuery("SELECT * FROM file_archive WHERE owner = ? OR accesslevel=3 OR accesslevel=2 AND ownerdept = ? ORDER BY id DESC ", FileArchive.class).
                    setParameter(1, id).setParameter(2, de.getDeptID().getId()).getResultList();
            List<Map> list = new ArrayList<>();

            for (FileArchive arr : att) {
                Map m = new HashMap();
                m.put("id", arr.getId());
                m.put("filename", arr.getFilename());
                m.put("description", arr.getDescription());
                m.put("relatedFile", arr.getRelatedfile());
                m.put("type", arr.getType());
                Employee e = em.find(Employee.class, arr.getOwner());
                m.put("uploadedBy", e.getFirstname() + " " + e.getLastname());
                m.put("lastModifiedDate", arr.getLastModifiedDate());
                m.put("dateUploaded", arr.getDateuploaded());
                m.put("fileDownload", UtilHelper.DOWNLOAD_PATH + arr.getFilepath());
                // boolean isOwner = Objects.equals(arr.getOwner(), id);
                m.put("isOwner", Objects.equals(arr.getOwner(), id));
                list.add(m);
            }

            return Response.ok(list).build();

        } catch (Exception e) {
            System.err.println("Error " + e.getMessage());

        }
        return Response.ok().build();
    }

    @GET
    @Path("byid/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findByFileId(@PathParam("id") Integer id) {

        try {

            FileArchive arr = em.find(FileArchive.class, id);
            Map m = new HashMap();
            m.put("id", arr.getId());
            m.put("filename", arr.getFilename());
            m.put("description", arr.getDescription());
            m.put("relatedFile", arr.getRelatedfile());
            m.put("type", arr.getType().toLowerCase());
            Employee e = em.find(Employee.class, arr.getOwner());
            m.put("uploadedBy", e.getFirstname() + " " + e.getLastname());
            m.put("lastModifiedDate", arr.getLastModifiedDate());
            m.put("dateUploaded", arr.getDateuploaded());
            m.put("fileDownload", UtilHelper.DOWNLOAD_PATH + arr.getFilepath());
            // boolean isOwner = Objects.equals(arr.getOwner(), id);
            m.put("isOwner", Objects.equals(arr.getOwner(), id));

            return Response.ok(m).build();

        } catch (Exception e) {
            System.err.println("Error " + e.getMessage());

        }
        return Response.ok().build();
    }

    @GET
    @Override
    @Produces({MediaType.APPLICATION_JSON})
    public List<FileArchive> findAll() {

        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<FileArchive> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
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

    @GET
    @Path("download/{id}")
    public Response downloadFile(@PathParam("id") int id) {

        FileArchive ar = em.find(FileArchive.class, id);
        if (ar != null) {
            File file = new File(UtilHelper.DOWNLOAD_PATH + ar.getFilepath());
            Response.ResponseBuilder response = Response.ok((Object) file);
            response.header("Content-Disposition", "attatchment; filename=\"" + file.getName() + "\"");
            return response.build();
        } else {
            return Response.noContent().build();
        }
    }
}
