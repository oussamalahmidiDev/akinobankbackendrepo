package com.akinobank.app.controllers.agent;

import com.akinobank.app.enumerations.Role;
import com.akinobank.app.models.*;
import com.akinobank.app.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/agent")
public class AgentPanelController {

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private AgenceRepository agenceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CompteRepository compteRepository;

//    ************************************************* API Agent profile ************************************************************

    @GetMapping(value = "/profile/{id}") // return Agent by id
    public Serializable getAgent(@PathVariable(value = "id")Long id){
        User user = userRepository.findById(id).get();
        if(user.getRole().equals(Role.AGENT)){
            return user;
        }
        else {
            return new RuntimeException("Agent not found").toString();
        }
    }

    //    *********************************************************************************************************************
    //    *****************************************************API Agence profile **********************************************

    @GetMapping(value = "/agence")
    public Agence getAgence(){
        Long id = 2L; //just a test , we will use the token to get agent id
        Agent agent= agentRepository.findById(id).get();
        Agence agence = agenceRepository.findById(agent.getAgence().getId()).get(); // get id agence from id agent
        return agence;
    }


    //    ************************************************************************************************************************
    //    ************************************************* API Show All clients ******************************************************


    @GetMapping(value = "/clients") //show all clients , works
    public List<User> getClients(){
        return userRepository.findAllByRole(Role.CLIENT);
    }

    //    ******************************************************************************************************************
    //    ***************************************************** API ADD Client **********************************************


    @PostMapping("/clients/ajouter") //add new client , works
    public User addClient(@RequestBody  User user){
        user.setRole(Role.CLIENT);
        //find first the client
        Agence agence = agenceRepository.findById(1L).get(); // i choose id 1 just for test
        Agent agent = agentRepository.findById(1L).get();  // i choose id 1 just for test
        userRepository.save(user); // add user in table
        Client client = new Client(user,agent ,agence);
        clientRepository.save(client); // add client in table
        return user;
    }

    //    *********************************************************************************************************************
    //    ***************************************************** API Delete Client **********************************************


    @DeleteMapping(value = "/clients/{id}/supprimer") // delete a client , works
    public String deleteClient(@PathVariable(value = "id") Long id){
        User user = userRepository.findById(id).get();
        if(user.getRole().equals(Role.CLIENT)){
        userRepository.deleteById(id);
        return "YOUR CLIENT with id =  "+id+" HES BEEN DELETED";}
        else{
            return "Client not found";
        }
    }

    //    **********************************************************************************************************************
    //    ***************************************************** API Modify Client **********************************************


    @PutMapping(value = "/clients/{id}/modifier") // modify client , works
    public Serializable modifyClient(@PathVariable("id") Long id , @RequestBody User user){
        try {
            user.setId(id); // specified the id and role of client you want to modify
            user.setRole(Role.CLIENT);
            User old_user = userRepository.findById(id).get(); // call the old user data
             Agent agent = agentRepository.findById(1L).get(); // just for test
             Agence agence = agenceRepository.findById(1L).get();
             user.setDateDeCreation(old_user.getDateDeCreation()); // get the first creation date and set it for the date creation
             userRepository.save(user); // save the modify info into user
             Client client = clientRepository.findClientByUserId(user.getId()); // search for client with the is user
             client.setAgent(agent); // save the agent
             client.setAgence(agence); //save the agence
             clientRepository.save(client);// save the new info of client
        return user;}
        catch (Exception e){
            return "Its not allowed";
        }
    }

    //    **********************************************************************************************************************
    //    ******************************************************** API get Client Comptes***************************************

    @GetMapping(value = "/clients/{id}/comptes") // works v2
    public Collection<Compte> getAllComptes(@PathVariable(value = "id")Long id) throws NoSuchElementException {
//        Agent agent = agentRepository.findById(1L).get(); // not necessary all agents can see all clients comptes : false
        //Agents can see their clients in the same agence
        try{
            if(clientRepository.findById(id).isPresent()) { //isPresent is the best solution to check the existance of an element in BD
                Client client = clientRepository.findById(id).get();
                return compteRepository.findAllByClientId(client.getId());
            }
            else{
                return null; // We will control it in the front if null then we need to genere a error msg as a span
            }
        }catch (Exception e){

            return null;
        }
    }

    //    **********************************************************************************************************************
    //    *********************************************************** API Add Client Compte *************************************

    @PostMapping(value = "/clients/{id}/comptes/ajouter") // works
    public Serializable addClientCompte(@PathVariable(value = "id")Long id,@RequestBody Compte compte){ // PS : if you didnt insert the solde , will be auto 0.0 and always its >0
        if(clientRepository.findById(id).isPresent()){ //check firstly if client exist
        Client client = clientRepository.findById(id).get();
        compte.setClient(client);
        return compteRepository.save(compte);}
        else {
            return null;
        }
    }

    //    **********************************************************************************************************************
    //    *********************************************************** API Delete Client Compte *********************************

    @DeleteMapping(value = "/comptes/{id}/delete") //works
    public String deleteClientCompte(@PathVariable(value = "id") String numero_compte){
        if(compteRepository.findById(numero_compte).isPresent()){
        Agent agent = agentRepository.findById(1L).get(); //just for test , choose the agent with id 2
        Compte compte =  compteRepository.findById(numero_compte).get();

//        System.out.println(compte);

        if(agent.getAgence().getId().equals(compte.getClient().getAgence().getId())){
        try{
            compteRepository.deleteById(numero_compte);
         return "The compte with number "+numero_compte+" HAS BEEN DELETED";}
        catch (Exception e){
            return e.toString();
        }}
        else {
            return "Agent is not allowed for this action";// if Agent is not from the same agence as client
        }}
        else {
            return "Compte Not Exist";
        }
    }
    //    **********************************************************************************************************************
    //    *********************************************************** API modify Client Compte *********************************

        @PutMapping(value = "/clients/comptes/{id}/modify")
    public Serializable modifyClientCompte(@PathVariable(value = "id") String numero_compte ,@RequestBody Compte compte) {
            if (compteRepository.findById(numero_compte).isPresent()) {

                Agent agent = agentRepository.findById(1L).get();//just for test , agent couldnt modify 2 comptes from diff agence
                Compte old_compte = compteRepository.findById(numero_compte).get();//for the creation date
                Client client = clientRepository.findById(old_compte.getClient().getId()).get();  // for the id Client
                compte.setNumeroCompte(numero_compte);
//            if(agent.getAgence().getId().equals(compte.getClient().getAgence().getId())){ // check if the agent work in the same agence
                //specify which id client
                compte.setClient(client);
                compte.setDateDeCreation(old_compte.getDateDeCreation());
                compteRepository.save(compte);
//            }

                return compte;

            } else {
                return null;
            }
        }
}
