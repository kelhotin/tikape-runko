package foorumi;

import foorumi.Sisalto.Aihe;
import foorumi.Sisalto.Alue;
import foorumi.Sisalto.Viesti;
import foorumi.collector.Aluekeraaja;
import foorumi.database.AiheDao;
import foorumi.database.AlueDao;
import foorumi.database.Database;
import foorumi.database.ViestiDao;
import java.util.*;
import java.sql.*;
import spark.*;
import static spark.Spark.*;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

public class Main {

    public static void main(String[] args) throws Exception {
        Database database = new Database("jdbc:sqlite:foorumi.db");
        database.setDebugMode(true);

        AiheDao aiheDao = new AiheDao(database, "jdbc:sqlite:foorumi.db");
        ViestiDao viestiDao = new ViestiDao(database, "jdbc:sqlite:foorumi.db");
        AlueDao alueDao = new AlueDao("jdbc:sqlite:foorumi.db");
        List<String> lista = viestiDao.findAikaViesteilleAiheesta("jääkiekko");
        System.out.println(lista.size());
//
        Spark.get("/", (req, res) -> {
            res.redirect("/etusivu");
            return "ok";
        });

        //etusivu, eli alueet listattu
        get("/etusivu", (req, res) -> {

            List<Alue> alueet = alueDao.findAll();
            HashMap map = new HashMap<>();
            List<String> ajat = new ArrayList<>();
            List<String> viestit = new ArrayList<>();
            for (Alue i : alueet) {
                String aika = viestiDao.findUusinAikaAlueelta(i.getNimi());
                ajat.add(aika);
                String maara = viestiDao.laskeViestitAlueelta(i.getNimi());
                viestit.add(maara);
            }
            map.put("maara", "viestejä:");
            map.put("teksti", "Alueet");
            map.put("alueet", alueet);
            map.put("ajat", ajat);
            map.put("viestit", viestit);

            return new ModelAndView(map, "index");
        }, new ThymeleafTemplateEngine());

        //tietty alue
        get("/alue/:id", (req, res) -> {
            HashMap<String, Object> map = new HashMap<>();
            String id = req.params(":id");
            List<Aihe> aiheet = aiheDao.findWithId(id);
            List<String> ajat = new ArrayList<>();
            List<String> viestit = new ArrayList<>();
            for (Aihe i : aiheet) {
                String aika = viestiDao.findUusinAikaAiheelta(i.getNimi());
                ajat.add(aika);
                String maara = viestiDao.laskeViestitAiheelta(i.getNimi());
                
                viestit.add(maara);
            }
            System.out.println(aiheet);
            map.put("aiheet", aiheet);
            map.put("ajat", ajat);
            map.put("viestit", viestit);
            return new ModelAndView(map, "alue");
        }, new ThymeleafTemplateEngine());

        //aiheen viestit;
        get("/aihe/:nimi", (req, res) -> {
            HashMap<String, Object> map = new HashMap<>();
            List<Viesti> viestit = viestiDao.findWithAihe(req.params(":nimi"));
            List<String> ajat = viestiDao.findAikaViesteilleAiheesta(req.params(":nimi"));
            
            map.put("viestit", viestit);
            map.put("ajat", ajat);
            return new ModelAndView(map, "viestit");
        }, new ThymeleafTemplateEngine());

        //lisää viesti
        Spark.post("/aihe/:nimi", (req, res) -> {
            viestiDao.lisaa(req.queryParams("viesti"), req.params(":nimi"));

            String nimi = req.params(":nimi");

            HashMap<String, Object> map = new HashMap<>();
            List<Viesti> viestit = viestiDao.findWithAihe(req.params(":nimi"));
            map.put("viestit", viestit);
            res.redirect("/aihe/" + req.params(":nimi"));
            return new ModelAndView(map, "viestit");
        }, new ThymeleafTemplateEngine());

        post("/alue/:nimi", (req, res) -> {     //luodaan aihe
            Alue alue = alueDao.findOne(req.params(":nimi"));
            aiheDao.lisaa(req.queryParams("aihe"), alue.getId() + " ");

            res.redirect("/alue/" + req.params(":nimi"));
            return "jee";
        });

        post("/etusivu", (req, res) -> {     //luodaan uusi alue
            alueDao.lisaa(req.queryParams("alue"));

            res.redirect("/");
            return "jee";
        });
    }
}