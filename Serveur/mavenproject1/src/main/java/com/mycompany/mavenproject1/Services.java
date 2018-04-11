/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.mavenproject1;

import generated.PallierType;
import generated.ProductType;
import generated.World;
import java.io.File;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author leman
 */
public class Services {
    
    JAXBContext jaxbContext;
    Unmarshaller jaxbUnmarshaller;
    World w;
    InputStream input;
    
    public World readWorldFromXml() throws JAXBException{
                    
        jaxbContext = JAXBContext.newInstance(World.class);
        jaxbUnmarshaller = jaxbContext.createUnmarshaller();

        input = getClass().getClassLoader().getResourceAsStream("world.xml");
        w = (World) jaxbUnmarshaller.unmarshal(input);
        
        return w;
    }
    
    public World readWorldFromXml(String user) throws JAXBException{
                    
        jaxbContext = JAXBContext.newInstance(World.class);
        jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        try {
            w = (World) jaxbUnmarshaller.unmarshal(new File(user+"-world.xml"));
        }catch(UnmarshalException e){
            input = getClass().getClassLoader().getResourceAsStream("world.xml");
            w = (World) jaxbUnmarshaller.unmarshal(input);
        }
        
        return w;
    }
    
    public void saveWorldToXml(World world){
        
        try {
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Marshaller m = cont.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );

            // Write the XML File
            m.marshal(world, new File("world.xml"));
        }catch (JAXBException e) {
            e.printStackTrace();
        }
    }
    
    public void saveWorldToXml(World world, String username) throws JAXBException {
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Marshaller m = cont.createMarshaller();
        m.marshal(world, new File(username+"-world.xml"));
    }
    
    public World getWorld(String username) throws JAXBException {
        World world = readWorldFromXml(username);
        calcNewScore(world);
        world.setLastupdate(System.currentTimeMillis());
        saveWorldToXml(world, username);
        return world;
    }
    
    private ProductType findProductById(World world, int id) {
        // id  -1 car commence à zero au lieu de 1
        return world.getProducts().getProduct().get(id-1);
    }
    
    private PallierType findManagerByName(World world, String name) {
        for(PallierType manager: world.getManagers().getPallier()) {
            if(manager.getName().equals(name)) {
                return manager;
            }
        }
        // Aucun manager de ce nom
        return null;
    }
    
    // prend en paramètre le pseudo du joueur et le produit 
    // sur lequel une action a eu lieu (lancement manuel de production ou  
    // achat d’une certaine quantité de produit) 
    // renvoie false si l’action n’a pas pu être traitée   
    public Boolean updateProduct(String username, ProductType newproduct) throws JAXBException { 
 
 
        // aller chercher le monde qui correspond au joueur         
        World world = getWorld(username); 
 
       // trouver dans ce monde, le produit équivalent à celui passé        
        // en paramètre         
        
        ProductType product = findProductById(world, newproduct.getId()); 
        if (product == null) { return false;}            
        // calculer la variation de quantité.
     
        int qtchange = newproduct.getQuantite() - product.getQuantite();
        
        //Si elle est positive c'est 
        //que le joueur a acheté une certaine quantité de ce produit    
        // sinon c’est qu’il s’agit d’un lancement de production.
        double croissance = product.getCroissance();
        if (qtchange > 0) {             
            // soustraire de l'argent du joueur le cout de la quantité achetée
            double depense = product.getCout()* qtchange * ((1-Math.pow(croissance, qtchange))/(1-croissance)); 
            world.setMoney(world.getMoney() - depense); 
            // et mettre à jour la quantité de product
            
            product.setQuantite(product.getQuantite() + qtchange);
            
            for(PallierType unlock: product.getPalliers().getPallier()) {
                /**
                 * on regarde le premier non débloqué
                 */
                if( ! unlock.isUnlocked()) { 
                    /**
                     * si on a assez de produits pour débloquer cet unlock
                     */
                    if(product.getQuantite() >= unlock.getSeuil()) {
                        /**
                         * on indique qu'il est débloqué
                         */
                        unlock.setUnlocked(true); 
                        /**
                         * on ajoute le bonus correspondant
                         */
                        switch(unlock.getTyperatio()) { 
                            case GAIN:
                                product.setRevenu(product.getRevenu() * unlock.getRatio());
                                break;
                            case VITESSE:
                                product.setVitesse((int) (product.getVitesse() / unlock.getRatio()));
                                product.setTimeleft((long) (product.getTimeleft() / unlock.getRatio()));
                                break;
                        }
                    }
                    else
                        break;
                }
            }
            
        } else {            
            // initialiser product.timeleft à product.vitesse          
            // pour lancer la production 
            product.setTimeleft(product.getVitesse());
 
        } 
 
        // sauvegarder les changements du monde
        calcNewScore(world);
        world.setLastupdate(System.currentTimeMillis());
        saveWorldToXml(world, username);         
        return true;    
    }
    
    // prend en paramètre le pseudo du joueur et le manager acheté. 
    // renvoie false si l’action n’a pas pu être traitée 
    public Boolean updateManager(PallierType newmanager, String username) throws JAXBException {
        // aller chercher le monde qui correspond au joueur 
        World world = getWorld(username);
         // trouver dans ce monde, le manager équivalent à celui passé      
         // en paramètre 
        PallierType manager = findManagerByName(world, newmanager.getName());
        if(manager == null) {
            return false;
        }
        // débloquer ce manager
        manager.setUnlocked(true);
        // trouver le produit correspond au manager
        ProductType product = findProductById(world, manager.getIdcible());
        if(product == null) {
            return false;
        }
        // débloquer le manager de ce produit
        product.setManagerUnlocked(true);
        // soustraire de l'argent du joueur le cout du manager
        world.setMoney(world.getMoney()-manager.getSeuil());
        
        //sauvegarder les changements au 
        calcNewScore(world);
        world.setLastupdate(System.currentTimeMillis());
        saveWorldToXml(world, username);
        return true;
    }
    
    public void calcNewScore(World w){
        long temp = System.currentTimeMillis();
        for(ProductType p : w.getProducts().getProduct()){
            if((p.isManagerUnlocked()) && (p.getQuantite() >= 1)){
                
                /*long quantiteProduite = temp - w.getLastupdate()/p.getVitesse();
                double argentwin = quantiteProduite * p.getQuantite() * p.getRevenu();
                
                w.setMoney(argentwin);
                w.setScore(argentwin);*/
                long lastUpd = w.getLastupdate();
                long t = Math.floorDiv(temp - lastUpd + p.getVitesse() - p.getTimeleft(), p.getVitesse());
                long restant = Math.floorMod(temp - lastUpd + p.getVitesse() - p.getTimeleft(), p.getVitesse());
                
                w.setMoney(w.getMoney()+ (p.getRevenu() *p.getQuantite() * (1+w.getActiveangels() * w.getAngelbonus()/100))*t);
                w.setScore(w.getMoney()+ (p.getRevenu() *p.getQuantite() * (1+w.getActiveangels() * w.getAngelbonus()/100))*t);
                
                p.setTimeleft(restant);
                if(p.getTimeleft() < 0){
                    p.setTimeleft(0);
                }
            }else{

                if( p.getTimeleft() > 0 && p.getTimeleft() <= temp - w.getLastupdate()){
                    w.setMoney(w.getMoney()+ (p.getRevenu() *p.getQuantite() * (1+w.getActiveangels() * w.getAngelbonus()/100)));
                    w.setScore(w.getMoney()+ (p.getRevenu() * p.getQuantite() * (1+w.getActiveangels() * w.getAngelbonus()/100)));
                    p.setTimeleft(0);
                }else if (p.getTimeleft() > 0){
                    p.setTimeleft(p.getTimeleft() - (temp - w.getLastupdate()));
                }
            }
             
        }
    }
    
    
}
