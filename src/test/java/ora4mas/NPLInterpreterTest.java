package ora4mas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import moise.os.OS;
import npl.DeonticModality;
import npl.NPLInterpreter;
import npl.NPLLiteral;
import npl.NormativeFailureException;
import npl.NormativeProgram;
import npl.Scope;
import npl.TimeTerm;
import npl.parser.ParseException;
import npl.parser.nplp;
import ora4mas.nopl.oe.Group;
import ora4mas.nopl.tools.os2nopl;

public class NPLInterpreterTest {

    @Before
    public void setUp() throws Exception {
        OS os = OS.loadOSFromURI("src/examples/writePaper/wp-os.xml");
        String np = os2nopl.transform(os);
        BufferedWriter out = new BufferedWriter(new FileWriter("src/examples/writePaper/wp-gen.npl"));
        out.write(np);
        out.close();
    }
    
    @Test
    public void testWPGroupHJouse() throws ParseException, Exception {
        String fName = "src/examples/test/house-os.xml";
        if (! new File(fName).exists()) {
            System.out.println("!!! not found: "+fName);
            return;
        }
        OS os = OS.loadOSFromURI(fName);
        String np = os2nopl.transform(os);
        
        Group g = new Group("g1");
        NormativeProgram p = new NormativeProgram();
        new nplp(new StringReader(np)).program(p,g);

        NPLInterpreter i = new NPLInterpreter();
        i.setScope(p.getRoot().getScope(ASSyntax.parseLiteral("group(house_group)")));
        
        assertTrue(i.holds(ASSyntax.parseLiteral("subrole(roofer,building_company)")));
        assertTrue(i.holds(ASSyntax.parseLiteral("tsubrole(roofer,building_company)")));
        assertTrue(i.holds(ASSyntax.parseLiteral("tsubrole(roofer,soc)")));
        assertFalse(i.holds(ASSyntax.parseLiteral("tsubrole(roofer,rrrr)")));
        assertFalse(i.holds(ASSyntax.parseLiteral("tsubrole(roofer,house_owner)")));
        assertTrue(i.holds(ASSyntax.parseLiteral("compatible(building_company,building_company,gr_inst)")));
        assertTrue(i.holds(ASSyntax.parseLiteral("fcompatible(building_company,building_company,gr_inst)")));
        assertTrue(i.holds(ASSyntax.parseLiteral("fcompatible(roofer,building_company,gr_inst)")));
        assertTrue(i.holds(ASSyntax.parseLiteral("fcompatible(roofer,window_fitter,gr_inst)")));
        assertFalse(i.holds(ASSyntax.parseLiteral("fcompatible(building_company,house_owner,gr_inst)")));
        assertFalse(i.holds(ASSyntax.parseLiteral("fcompatible(roofer,house_owner,gr_inst)")));
        
        // change OE
        
        g.addPlayer("a","plumber");
        g.addPlayer("b","house_owner");
        g.addPlayer("c","site_prep_contractor");
        g.addPlayer("d","bricklayer");
        g.addPlayer("e","roofer");
        g.addPlayer("f","window_fitter");
        g.addPlayer("g","door_fitter");
        g.addPlayer("h","electrician");
        g.addPlayer("i","painter");
                        
        assertTrue(i.holds(new NPLLiteral(ASSyntax.parseLiteral("play(_,electrician,g1)"), g)));
        assertTrue(i.holds(ASSyntax.parseLiteral("rplayers(electrician,g1,1)")));
        assertTrue(i.holds(ASSyntax.parseLiteral("well_formed(g1)")));

    }
    
    @Test
    public void testWPGroup1() throws ParseException, Exception {
        NormativeProgram p = new NormativeProgram();
        String src = "src/examples/writePaper/wp-gen.npl";
        Group g = new Group("g1");
        p.setSrc(src);
        new nplp(new FileReader(src)).program(p,g);

        NPLInterpreter i = new NPLInterpreter();
        i.setScope(p.getRoot().getScope(ASSyntax.parseLiteral("group(wpgroup)")));
        
        assertTrue(i.holds(ASSyntax.parseLiteral("role_cardinality(writer,1,5)")));
        assertFalse(i.holds(ASSyntax.parseLiteral("role_cardinality(writer,1)")));

        assertFalse(i.holds(ASSyntax.parseLiteral("well_formed(\"g1\")")));

        assertTrue(i.holds(ASSyntax.parseLiteral("tsubrole(editor,editor)")));
        assertTrue(i.holds(ASSyntax.parseLiteral("tsubrole(writer,writer)")));
        assertTrue(i.holds(ASSyntax.parseLiteral("compatible(editor,writer,gr_inst)")));
        assertTrue(i.holds(ASSyntax.parseLiteral("compatible(writer,editor,gr_inst)")));
        assertTrue(i.holds(ASSyntax.parseLiteral("fcompatible(editor,writer,gr_inst)")));
        assertTrue(i.holds(ASSyntax.parseLiteral("fcompatible(writer,editor,gr_inst)")));
        
        // add players
        g.addPlayer("jaime", "editor");
        g.addPlayer("olivier", "writer");
        assertTrue(i.holds(ASSyntax.parseLiteral("well_formed(g1)")));
        
        // change OE
        g.addPlayer("jomi", "editor");
        assertFalse(i.holds(ASSyntax.parseLiteral("well_formed(g1)")));
        g.removePlayer("jomi", "editor");
        g.addPlayer("jomi", "writer");
        assertTrue(i.holds(ASSyntax.parseLiteral("well_formed(g1)")));
                
        assertTrue(i.holds(ASSyntax.parseLiteral("fplay(jaime,editor,g1)")));
        assertTrue(i.holds(ASSyntax.parseLiteral("fplay(jaime,author,g1)")));
        assertTrue(i.holds(ASSyntax.parseLiteral("fplay(jaime,soc,g1)")));
        assertFalse(i.holds(ASSyntax.parseLiteral("fplay(jaime,writer,g1)")));
        
        assertFalse(i.holds(ASSyntax.parseFormula("play(jaime,R1,Gr) & play(jaime,R2,Gr) & R1 < R2 & not compatible(R1,R2)")));
    }

    @Test
    public void testWPGroup2() throws ParseException, Exception {
        OS os = OS.loadOSFromURI("src/examples/auction/auction-os.xml");
        String np = os2nopl.transform(os);

        Group g = new Group("g1");
        
        NormativeProgram p = new NormativeProgram();
        new nplp(new StringReader(np)).program(p, g);

        NPLInterpreter i = new NPLInterpreter();
        i.setScope(p.getRoot().getScope(ASSyntax.parseLiteral("group(auctionGroup)")));
        
        // add players
        g.addPlayer("jaime", "auctioneer");
        g.addPlayer("olivier", "participant");
        assertTrue(i.holds(ASSyntax.parseLiteral("well_formed(g1)")));
        
        // test compatibility
        assertFalse(i.holds(ASSyntax.parseFormula("tcompatible(auctioneer,participant,gr_inst)")));
        assertTrue(i.holds(ASSyntax.parseFormula("fcompatible(auctioneer,soc,gr_inst)")));
        assertFalse(i.holds(ASSyntax.parseFormula("fcompatible(soc,auctioneer,gr_inst)")));

        assertTrue(i.holds(ASSyntax.parseFormula("fcompatible(participant,soc,gr_inst)")));
        assertFalse(i.holds(ASSyntax.parseFormula("fcompatible(soc,participant,gr_inst)")));
        
        assertFalse(i.holds(ASSyntax.parseFormula("fcompatible(auctioneer,participant,gr_inst)")));
        assertFalse(i.holds(ASSyntax.parseFormula("fcompatible(participant,auctioneer,gr_inst)")));
        
    }
    
    @Test
    public void testWPGroupVerify() throws ParseException, Exception {
        NormativeProgram p = new NormativeProgram();
        String src = "src/examples/writePaper/wp-gen.npl";
        p.setSrc(src);
        Group g = new Group("wp1");
        new nplp(new FileReader(src)).program(p, g);

        NPLInterpreter i = new NPLInterpreter();
        Scope wp = p.getRoot().getScope(ASSyntax.parseLiteral("group(wpgroup)"));
        i.setScope(wp);
        
        g.addPlayer("jaime", "editor");
        g.addPlayer("olivier", "writer");
        g.addPlayer("jomi", "writer");
        assertTrue(i.holds(ASSyntax.parseLiteral("well_formed(wp1)")));
        assertEquals(0, i.verifyNorms().size());
        
        // simulates a role that cannot be played
        g.addPlayer("bob", "role1");
        assertTrue(i.holds(ASSyntax.parseLiteral("well_formed(wp1)")));
        try {
            i.verifyNorms();
            assertTrue(false); // should throw exception
        } catch (NormativeFailureException e) { 
            assertEquals("role_in_group(bob,role1,wp1)", e.getFail().getTerm(0).toString());
        }
        g.removePlayer("bob", "role1");
        
        
        // simulates cardinality failure
        g.addPlayer("bob","editor");
        assertFalse(i.holds(ASSyntax.parseLiteral("well_formed(wp1)")));
        try {
            i.verifyNorms();
            assertTrue(false); // should throw exception
        } catch (NormativeFailureException e) { 
            assertEquals("role_cardinality(editor,wp1,2,1)", e.getFail().getTerm(0).toString());
        }
        g.removePlayer("bob","editor");
        
        // simulates compatibility obligation
        g.addPlayer("jaime","writer");
        i.verifyNorms(); // no problem here, roles are compatible
        g.addPlayer("jaime","anotherrole");
        
        assertTrue(wp.removeNorm("role_in_group") != null); // remove this norm to allows jaime to adopt the new role
        assertTrue(wp.removeNorm("role_compatibility") != null);
        i.setScope(wp);
        @SuppressWarnings("unused")
        Collection<DeonticModality> rver = i.verifyNorms();
        //System.out.println(rver);

        // simulates responsible for
        g.addResponsibleForScheme("tutu");
        assertTrue(g.removePlayer("jaime", "editor"));
        //System.out.println(oe2np.group2np(gtmp));

        try {
            i.verifyNorms();
            assertTrue(false); // should throw exception
        } catch (NormativeFailureException e) { 
            assertEquals("well_formed_responsible(wp1)", e.getFail().getTerm(0).toString());
        }
    }
    
    @Test
    public void testWPSchemeVerify() throws ParseException, Exception {
        //for (Literal l: oi.getNPLI().getAg().getBB())
        //    System.out.println(l);

        // create OE
        Group g = new Group("wp1");
        g.addPlayer("jaime", "editor");
        g.addPlayer("olivier", "writer");
        g.addPlayer("jomi", "writer");
        g.addResponsibleForScheme("sch2");
        assertEquals(3, g.getPlayers().size());
        OI oi = new OI("src/examples/writePaper/wp-os.xml", "writePaperSch", "sch2");
        oi.getScheme().addGroupResponsibleFor(g);
        oi.setGroup(g);

        assertFalse(oi.getNPLI().holds(ASSyntax.parseLiteral("is_finished(\"sch2\")")));
        

        // mission obligation
        oi.getNPLI().setUpdateInterval(100);
        /*oi.getNPLI().addListener(new DefaultNormativeListener() {
            @Override
            public void created(DeonticModality o) {
                System.out.println("created "+o);
            }
            @Override
            public void inactive(DeonticModality o) {
                System.out.println("inactive "+o);
            }
        });*/
        Collection<DeonticModality> rver = oi.getNPLI().verifyNorms();
        //System.out.println(oi.getNPLI().getSource(NPLInterpreter.OEAtom));
        int cobl = 0; int cperm = 0;
        for (Literal l: rver) {
            //System.out.println(l);
            assertEquals(4,l.getArity());
            if (NormativeProgram.OblFunctor.equals(l.getFunctor())) cobl++;
            if (NormativeProgram.PerFunctor.equals(l.getFunctor())) cperm++;
        }
        assertEquals(4, cobl);
        assertEquals(1, cperm);
        Thread.sleep(1100);
        
        assertEquals(4,oi.getNPLI().getActiveObligations().size());
        assertEquals(1,oi.getNPLI().getActivePermissions().size());
        assertEquals(0,oi.getNPLI().getUnFulfilledObligations().size());
        assertEquals(0,oi.getNPLI().getFulfilledObligations().size());
        assertEquals(0,oi.getNPLI().getInactiveObligations().size());
        
        // mission permission
        //assertTrue(oi.getNPLI().holds(ASSyntax.parseLiteral("permitted(jaime,nc3,committed(jaime,mman,S),_)")));
        
        // test some rules
        assertFalse(oi.getNPLI().holds(ASSyntax.parseLiteral("well_formed(\"sch2\")")));

        assertTrue(oi.getNPLI().holds(ASSyntax.parseLiteral("mission_role(mManager,editor)")));

        // change to well formed
        oi.execute(ASSyntax.parseLiteral("commitMission(jaime,mManager)"));
        Literal r = oi.execute(ASSyntax.parseLiteral("commitMission(jomi,ma)"));
        assertEquals("fail(mission_permission(jomi,ma,sch2))",r.toString());
        assertNull(oi.execute(ASSyntax.parseLiteral("commitMission(jomi,mColaborator)")));        
        assertNull(oi.execute(ASSyntax.parseLiteral("commitMission(jomi,mBib)")));
        
        assertTrue(oi.getNPLI().holds(ASSyntax.parseLiteral("well_formed(sch2)")));
        assertTrue(oi.getNPLI().holds(ASSyntax.parseLiteral("well_formed(sch2)")));

        assertTrue(oi.getNPLI().holds(ASSyntax.parseLiteral("mplayers(mBib,sch2,1)")));
        assertFalse(oi.getNPLI().holds(ASSyntax.parseLiteral("mplayers(mBib,sch2,0)")));
        //System.out.println("2. "+oi.getNPLI().getSource(NPLInterpreter.OEAtom));
        //Iterator<Unifier> iu = ASSyntax.parseFormula("play(A,writer,G) & responsible(G,S) & players(mbib,S,V) & scheme_mission(mbib,Min,_) & V < Min").logicalConsequence(oi.getNPLI().getAg(), new Unifier());
        //System.out.println("2. "+iu.next());

        assertTrue(oi.getNPLI().holds(ASSyntax.parseLiteral("enabled(sch2,wtitle)")));
        assertFalse(oi.getNPLI().holds(ASSyntax.parseLiteral("enabled(sch2,finish)")));
        assertFalse(oi.getNPLI().holds(ASSyntax.parseLiteral("enabled(sch2,sv)")));
        assertFalse(oi.getNPLI().holds(ASSyntax.parseLiteral("satisfied(sch2,sv)")));
        assertFalse(oi.getNPLI().holds(ASSyntax.parseLiteral("enabled(sch2,wp)")));

        Thread.sleep(200);
        List<DeonticModality> obls = oi.getNPLI().getActiveObligations();
        for (DeonticModality o: obls) {
            //System.out.println(o);
            assertEquals(4,o.getArity());
            assertEquals(NormativeProgram.OblFunctor, o.getFunctor());
            if (o.getTerm(0).toString().equals("jaime")) {
                //assertEquals("ngoal(sch2,mManager,wtitle)",o.getTerm(1).toString());
                assertEquals("done(sch2,wtitle,jaime)",o.getTerm(2).toString());
            }
        }
        assertEquals(1,obls.size());
        
        // jomi fulfilled 2 obl, olivier 2
        //assertEquals(4, oi.getNPLI().getInactiveObligations().size());
        
        // test cardinality (in this case cause an obligation)
        /*
         *         assertTrue(oi.getNPLI().holds(ASSyntax.parseLiteral("mplayers(mBib,_,1)")));


        System.out.println( oi.getNPLI().getActiveObligations());
        r = oi.execute(ASSyntax.parseLiteral("commitMission(olivier,mBib)"));
        System.out.println(r);
        //assertEquals("fail(mission_cardinality(mBib,sch2,2,1))", r.toString());
        System.out.println( oi.getNPLI().getActiveObligations());
        */ 
        
        // olivier commits to mission col
        assertNull(oi.execute(ASSyntax.parseLiteral("commitMission(olivier,mColaborator)")));
        
        // olivier leaves mission col
        r = oi.execute(ASSyntax.parseLiteral("leaveMission(olivier,mColaborator)"));
        assertEquals("fail(mission_left(olivier,mColaborator,sch2))", r.toString());
        
        // there must be one obligation: jaime for its goal
        Thread.sleep(300);  
        obls = oi.getNPLI().getActiveObligations();
        assertEquals(1,obls.size());
        
        assertNull(oi.execute(ASSyntax.parseLiteral("commitMission(olivier,mColaborator)")));
        obls = oi.getNPLI().getActiveObligations();
        assertEquals(1,obls.size());
        
        // jaime do setgoalachieved
        r = oi.execute(ASSyntax.parseLiteral("setGoalAchieved(bob,wtitle)"));
        assertNotNull(r);
        assertEquals("fail(ach_not_committed_goal(sch2,wtitle,bob))", r.toString());
        assertNull(oi.execute(ASSyntax.parseLiteral("setGoalAchieved(jaime,wtitle)")));
        // only one obligation: wabs
        Thread.sleep(300);  
        obls = oi.getNPLI().getActiveObligations();
        assertEquals(1,obls.size());
        assertEquals("done(sch2,wabs,jaime)", obls.get(0).getTerm(2).toString());

        // other goals of jaime
        System.out.println("abs");
        assertNull(oi.execute(ASSyntax.parseLiteral("setGoalAchieved(jaime,wabs)")));
        assertNull(oi.execute(ASSyntax.parseLiteral("setGoalAchieved(jaime,wsectitles)")));
        
        assertFalse(oi.getNPLI().holds(ASSyntax.parseLiteral("enabled(sch2,fdv)")));
        assertTrue(oi.getNPLI().holds(ASSyntax.parseLiteral("enabled(sch2,wsecs)")));
        assertTrue(oi.getNPLI().holds(new NPLLiteral(ASSyntax.parseLiteral("satisfied(sch2,fdv)"), oi.getScheme())));
        Thread.sleep(300);  
        obls = oi.getNPLI().getActiveObligations();
        assertEquals(2,obls.size()); // obls for jomi and olivier
        Collections.sort(obls);
        assertEquals("done(sch2,wsecs,jomi)", obls.get(0).getTerm(2).toString());
        assertEquals("done(sch2,wsecs,olivier)", obls.get(1).getTerm(2).toString());

        assertNotNull(oi.execute(ASSyntax.parseLiteral("setGoalAchieved(jaime,wconc)"))); // the goal is not enabled yet

        // wsec is satisfied by jomi
        System.out.println("wsecs 1");
        assertNull(oi.execute(ASSyntax.parseLiteral("setGoalAchieved(jomi,wsecs)")));
        assertNotNull(oi.execute(ASSyntax.parseLiteral("setGoalAchieved(jaime,wconc)"))); // the goal is not enabled yet, jomi AND olivier have to achieve wsec

        // wsec is satisfied also by olivier
        System.out.println("wsecs 2");
        r = oi.execute(ASSyntax.parseLiteral("setGoalAchieved(olivier,wsecs)"));
        assertNull(r);

        System.out.println("wrefs");
        assertNull(oi.execute(ASSyntax.parseLiteral("setGoalAchieved(jomi,wrefs)")));

        // jomi will leave the scheme before it is finished, since he fulfilled all its goals
        assertTrue(oi.getNPLI().holds(new NPLLiteral(ASSyntax.parseLiteral("satisfied(sch2,wsecs)"), oi)));
        assertTrue(oi.getNPLI().holds(ASSyntax.parseLiteral("all_satisfied(sch2,[wsecs])")));
        assertTrue(oi.getNPLI().holds(ASSyntax.parseLiteral("mission_accomplished(sch2,mColaborator)")));
        assertTrue(oi.getNPLI().holds(ASSyntax.parseLiteral("mission_accomplished(sch2,mBib)")));
        assertFalse(oi.getNPLI().holds(ASSyntax.parseLiteral("mission_accomplished(sch2,mManager)")));
        assertTrue(oi.getNPLI().holds(new NPLLiteral(ASSyntax.parseLiteral("committed(jaime,mManager,sch2)"), oi)));
        assertTrue(oi.getNPLI().holds(ASSyntax.parseLiteral("enabled(sch2,wabs)")));

        r = oi.execute(ASSyntax.parseLiteral("leaveMission(jomi,mBib)"));
        assertNull(r);
        assertNull(oi.execute(ASSyntax.parseLiteral("leaveMission(jomi,mColaborator)")));

        // others goals
        System.out.println("wconc");
        assertNull(oi.execute(ASSyntax.parseLiteral("setGoalAchieved(jaime,wconc)")));

        assertTrue(oi.getNPLI().holds(ASSyntax.parseLiteral("enabled(sch2,wp)")));

        // jaime waits a lot to finish the scheme
        String ob1 = oi.getNPLI().getActiveObligations().toString();
        //System.out.println(ob1);
        Thread.sleep(6000);
        String ob2 = oi.getNPLI().getActiveObligations().toString();
        // the time change the obligation (the NPI automatically update that)
        //System.out.println(ob2);
        assertFalse(ob1.toString().equals(ob2.toString()));

        List<DeonticModality> unful = oi.getNPLI().getUnFulfilledObligations();
        //System.out.println("Unful: "+unful);
        assertTrue(unful.size() >= 1);
        //assertEquals("achieved(\"sch2\",wp,jaime)", unful.get(0).getTerm(2).toString());
        
        assertNull(oi.execute(ASSyntax.parseLiteral("setGoalAchieved(jaime,wp)")));
        
        Thread.sleep(300);  
        obls = oi.getNPLI().getActiveObligations();
        assertEquals(0,obls.size()); // no more obligations
        assertTrue(oi.getNPLI().getUnFulfilledObligations().size() >= 1); // the jaime wp goal
        //assertEquals(11, oi.getNPLI().getFulfilledObligations().size());
        //assertEquals(1, oi.getNPLI().getInactiveObligations().size());
        
        assertTrue(oi.getNPLI().holds(ASSyntax.parseLiteral("is_finished(sch2)")));
        assertTrue(oi.getNPLI().holds(new NPLLiteral(ASSyntax.parseLiteral("satisfied(sch2,wp)"), oi)));
        assertTrue(oi.getNPLI().holds(ASSyntax.parseLiteral("mission_accomplished(sch2,mManager)")));
        
        // leave missions
        r = oi.execute(ASSyntax.parseLiteral("leaveMission(olivier,mColaborator)"));
        assertNull(r);
        assertNull(oi.execute(ASSyntax.parseLiteral("leaveMission(jaime,mManager)")));
        obls = oi.getNPLI().getActiveObligations();
        assertEquals(0,obls.size()); // no more obligations
    }
    
    /*
    public void testMonitorSch() throws Exception {
        OS os = OS.loadOSFromURI("examples/writePaper/wp-os.xml");
        String np = os2np.transform(os);
        NormativeProgram p = new NormativeProgram();
        new nplp(new StringReader(np)).program(p);
        NPLInterpreter engine = new NPLInterpreter();
        Scope gr = p.getRoot().findScope("group(wpgroup)");
        Scope schscope = gr.findScope("scheme(monitoringSch)");
        engine.setScope( schscope );
        assertEquals("group(wpgroup)", engine.getScope().getFather().getId().toString() );
        assertTrue( engine.holds( ASSyntax.parseLiteral("role_cardinality(editor,1,1)")));
        
        Group g = new Group("wp1");
        g.addPlayer("jaime", "editor");
        g.addPlayer("olivier", "writer");
        g.addPlayer("jomi", "writer");
        g.addResponsibleForScheme("msch");
        g.setMonitorSch("msch");
        
        Scheme sch = new Scheme("msch");
        sch.addGroupResponsibleFor(g);
        engine.setDynamicFacts( oe2np.transform(sch) );
        engine.verifyNorms();
        
        g.addPlayer("jomi", "editor");
        engine.setDynamicFacts( oe2np.transform(sch) );
        assertTrue( engine.holds(ASSyntax.parseLiteral("group_id(wp1) ")));
        assertTrue( engine.holds(ASSyntax.parseLiteral("role_cardinality(editor,_,1)")));
        assertTrue( engine.holds(ASSyntax.parseLiteral("rplayers(editor,wp1,2)")));
        assertTrue( engine.holds(ASSyntax.parseLiteral("play(A,editor,wp1)")));
        assertTrue( engine.holds(ASSyntax.parseLiteral("scheme_id(msch)")));
        assertTrue( engine.holds(ASSyntax.parseLiteral("responsible(wp1,msch)")));
        assertTrue( engine.holds(ASSyntax.parseLiteral("mplayers(ms,msch,0)")));
        
        assertTrue( engine.holds( engine.getNorm("n9").getCondition() ));
        assertEquals(2, engine.verifyNorms()[0].size() );
        
        // test obligations in scheme monitor
        gr = p.getRoot().findScope("scheme(writePaperSch)");
        schscope = gr.findScope("scheme(monitoringSch)");
        engine.setScope( schscope );

        Scheme s1 = new Scheme("s1");
        g.addResponsibleForScheme("s1");
        s1.addGroupResponsibleFor(g);
        Scheme monSch = new Scheme("mm");
        monSch.setMonitoredSch(s1);
        
        engine.setDynamicFacts( oe2np.transform(monSch));
        engine.verifyNorms();

        s1.addPlayer("olivier", "mColaborator");
        s1.addPlayer("jomi", "mBib");
        engine.setDynamicFacts( oe2np.transform(monSch));
        engine.verifyNorms();
        //System.out.println(engine.getFulfilledObligations());
        //System.out.println(engine.getInactiveObligations());
        
    }*/
    
    @Test
    public void testTimeTerm() throws Exception {
        TimeTerm t1 = new TimeTerm(1,"day");
        TimeTerm t2 = new TimeTerm(24,"hours");
        assertEquals(t1,t2);
        t1 = new TimeTerm(0,"now");
        //long v1 = (long)t1.solve();
        assertEquals(t1,new TimeTerm(0,"now"));
        assertFalse(t2.equals(t1));
        Thread.sleep(200);
        assertEquals(t1,new TimeTerm(0,"now"));
        t1 = (TimeTerm)t1.capply(null);
        Thread.sleep(200);
        assertFalse(t1.equals(new TimeTerm(0,"now")));
        
        //long v2 = (long)t1.solve();
        //assertFalse(v1 == v2);
        
        TimeTerm n = new TimeTerm(0,"never");
        assertEquals(n,new TimeTerm(0,"never"));
        assertFalse(n.equals(t1));
        assertFalse(n.equals(t2));
        
        assertEquals("now + 1 days", TimeTerm.toRelTimeStr( (long) t2.solve() + System.currentTimeMillis()));
        assertEquals("now", TimeTerm.toRelTimeStr( (long) t1.solve()));
        
        TimeTerm t3 = new TimeTerm(3,"seconds");
        assertEquals("now + 3 seconds", TimeTerm.toRelTimeStr( (long) t3.solve() + System.currentTimeMillis()));
        
        t1 = new TimeTerm(0, null);
        Thread.sleep(100);
        t2 = (TimeTerm)t1.clone();
        assertEquals(t1,t2);
    }    
}
