package com.drekerd.apitestmockup;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/users")
public class Controller {

    private static final String API_SECRET = "Yn2kjibddFAWtnPJ2AFlL8WXmohJMCvigQggaEypa5E=";
    private final String API_KEY = "b75a18ac-b25d-11ea-b3de-0242ac130004";

    private Date date = new Date();
    private long id;

    private boolean mIsUserValidToCreate =false;

    List<User> users = new ArrayList<>();

    private String role = "";

    @PostConstruct
    private void generateUserAdmin(){
        User user = new User();
        user.setId(1);
        user.setName("Mário");
        user.setRole("ADMIN");
        user.setNif("256757674");
        users.add(user);
    }

    @GetMapping("/hello/{name}")
    public String helloTest(@PathVariable String name){
        if(name.equalsIgnoreCase("pedro")){
            File file = new File("src/main/resources/readme");

            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            String st = "";
            StringBuilder builder = new StringBuilder();
            while (true) {
                try {
                    if (!((st = br.readLine()) != null))break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                builder.append(st+ "\n");
                System.out.println(st);
            }
            return "Hello Pedro, Welcome to this api testing\nToday is: " + LocalTime.now()+ "\n"+ builder;

    }else
            return "You are not Pedro!";
    }


    @GetMapping("")
    public ResponseEntity<Object> getAllUsers(HttpServletRequest request){
        if(!apiKeyExist(request)){
            return new ResponseEntity<>("Api Key missing or invalid", HttpStatus.BAD_REQUEST);
        }

        try{
            if(!jwtIsValid(request)){
                return new ResponseEntity<>("jwt Key missing or invalid", HttpStatus.BAD_REQUEST);
            }
        }catch (Exception e){
            return new ResponseEntity<>("A problem occured with your jwt. Check if it is still valid", HttpStatus.BAD_REQUEST);
        }

        if(this.role.equals("ADMIN")){
            log.info("getAllUsers Enter");
            return new ResponseEntity<>(users, HttpStatus.OK);
        }
        return new ResponseEntity<>("not authorized", HttpStatus.FORBIDDEN);
    }

    @RequestMapping(value="/user", method = RequestMethod.GET)
    public ResponseEntity<Object> getuserByid(@RequestParam("id") long id, HttpServletRequest request){
        log.info("getUser Enter");

        if(!apiKeyExist(request)){
            return new ResponseEntity<>("Api Key missing or invalid", HttpStatus.BAD_REQUEST);
        }

        if(!jwtIsValid(request)){
            return new ResponseEntity<>("jwt Key missing or invalid", HttpStatus.BAD_REQUEST);
        }

        if(this.role.equals("ADMIN")){
            log.info("Search for Id:" + id);
            return isUsersExist(id);
        }

        return new ResponseEntity<>("not authorized", HttpStatus.FORBIDDEN);
    }

    @PostMapping("user/create")
    public ResponseEntity<String> createUser(@RequestBody User user, HttpServletRequest request){

        if(!apiKeyExist(request)){
            return new ResponseEntity<>("Api Key missing or non existing", HttpStatus.BAD_REQUEST);
        }

        log.info("createUser Enter");
        ResponseEntity responseEntity = isUserValid(user);
        if(mIsUserValidToCreate){
            addUser(user);
        };
        log.info("createUser Exit");
        return responseEntity;
    }

    @PutMapping("user/update")
    public ResponseEntity<Object> updateUser(@RequestBody User user, HttpServletRequest request){

        if(!apiKeyExist(request)){
            return new ResponseEntity<>("Api Key missing or non existing", HttpStatus.BAD_REQUEST);
        }

        log.info("iD: " + user.getId());
        if(Long.valueOf(user.getId()) == null){
            log.info("Error updating the user, parsing name");
            return new ResponseEntity<>("Id cannot be empty", HttpStatus.BAD_REQUEST);
        }

        mIsUserValidToCreate = false;
        ResponseEntity<Object> responseEntityNotExist = isUserValid(user);

        if(mIsUserValidToCreate){
            return updateUserList(user);
        }

        return responseEntityNotExist;
    }

    @DeleteMapping("user/delete")
    public ResponseEntity<Object> deleteUser(@RequestBody User user, HttpServletRequest request){

        if(!apiKeyExist(request)){
            return new ResponseEntity<>("Api Key missing or non existing", HttpStatus.BAD_REQUEST);
        }

        if(Long.valueOf(user.getId()) == null || Long.valueOf(user.getId()).toString() == ""){
            log.info("Error updating the user, parsing name");
            return new ResponseEntity<>("Id cannot be empty", HttpStatus.BAD_REQUEST);
        }

        return deleteUserByid(user.getId());
    }

    private ResponseEntity<Object> deleteUserByid(long id) {
        for(User user : users){
            if(Long.valueOf(user.getId())==Long.valueOf(id)){
                log.info("ID FROM ARRAY: " + user.getId());
                log.info("ID FROM HTTP: " + id);
                users.remove(user);
                return new ResponseEntity<>("User Removed:" + user, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>("User with the id: " + id +" does not exist", HttpStatus.BAD_REQUEST);
    }

    private boolean apiKeyExist(HttpServletRequest request) {
        log.info("Validating ApiKey");
        if(request.getHeader("apikey") !=null) {
            if (request.getHeader("apiKey").equals(API_KEY)) {
                log.info("apiKey : OK");
                return true;
            }
        }
        log.error("apiKey missing or invalid");
        return false;
    }

    private ResponseEntity<Object> isUserValid(User user) {

        log.info("is UserValid ENTER");
        log.info("user:" + user.toString());

        if(user.getName().isBlank() || user.getName().isEmpty()){
            log.info("Error creating the user, parsing name");
            return new ResponseEntity<>("Name empty or invalid", HttpStatus.BAD_REQUEST);
        }

        if(user.getRole().isBlank() || user.getRole().isEmpty()){
            log.info("Error creating the user, parsing role");
            return new ResponseEntity<>("User Role empty or invalid", HttpStatus.BAD_REQUEST);
        }else{
            if((user.getRole().equalsIgnoreCase("ADMIN") == false) && (user.getRole().equalsIgnoreCase("USER") == false)){
                log.info("Error creating the user, parsing role");
                return new ResponseEntity<>("User Role empty or invalid", HttpStatus.BAD_REQUEST);
            }
        }

        if(user.getNif().isBlank() || user.getNif().isEmpty()){
            log.info("Error creating the user, parsing NIF");
            return new ResponseEntity<>("User Nif empty or invalid", HttpStatus.BAD_REQUEST);
        }else{
            if (!user.getNif().matches("[0-9]+") && user.getNif().length() < 2){
                log.info("Error creating the user, parsing NIF");
                return new ResponseEntity<>("User Nif empty or invalid", HttpStatus.BAD_REQUEST);
            }
        }
        log.info("USER IS VALID: " +  user.toString());
        log.info("is UserValid EXIT");
        mIsUserValidToCreate = true;

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    private ResponseEntity<Object> isUsersExist(long id) {
        for(User user : users){
            if(user.getId()==id){
                return new ResponseEntity<>(user, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>("User with the id: " + id +" does not exist", HttpStatus.BAD_REQUEST);
    }

    private void addUser(User user) {
        log.info("addUser Enter");

        id += 1;
        user.setId(id);
        User newUser = new User();
        newUser.setId(user.getId());
        newUser.setName(user.getName());
        newUser.setNif(user.getNif());
        newUser.setRole(user.getRole());

        users.add(newUser);

        log.info("addUser Exit");
    }

    private ResponseEntity<Object> updateUserList(User userToUpdate) {

        for(User user : users){
            if(user.getId() == userToUpdate.getId()){
                user.setId(userToUpdate.getId());
                user.setName(userToUpdate.getName());
                user.setNif(userToUpdate.getNif());
                user.setRole(userToUpdate.getRole());
                return new ResponseEntity<>(user, HttpStatus.ACCEPTED);
            }
        }
        return new ResponseEntity<>("User with the id: " + id +" does not exist", HttpStatus.BAD_REQUEST);
    }

    private boolean jwtIsValid(HttpServletRequest request) {
        this.role = "";
        log.info("Validating JWT");
        if(request.getHeader("jwt") !=null && !request.getHeader("jwt").isEmpty()) {
            String jwt = request.getHeader("jwt");
            if(jwt.indexOf('.', jwt.indexOf('.') + 1) != -1) {
            Claims claims = Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(API_SECRET))
                    .parseClaimsJws(request.getHeader("jwt")).getBody();

            if(claims.getSubject().equals("ADMIN") || claims.getSubject().equals("USER")) {
                this.role = claims.getSubject();
            }
            log.info("CLAIMS: " + claims);
                return true;
            }
        }
        log.error("jwt missing or invalid");
        return false;
    }
}
