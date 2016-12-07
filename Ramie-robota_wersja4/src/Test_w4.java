import javax.media.j3d.*;
import javax.sound.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.Period;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.UnsupportedAudioFileException;


import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.universe.SimpleUniverse;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

public class Test_w4 extends JFrame implements KeyListener {

	/*
	 * DLA MAREZA !! Pamietaj ze wspolrzedne to sa wspolrzedne srodka figury.
	 * Czyli jezeli cos ma wymiary o dlugosci 0.4f to tak naprawde caly bok ma
	 * dlugosc 0.8f zmienna wspolrzednaY to wspolrzedna Y obu czlonów robota
	 * zmienna wspolrzednaZ to wspolrzedne polozenia Z czlonu
	 * wysuwajacego/wsuwajacego. Wspolrzednymi X nie poruszamy wogóle wsumie.
	 * 
	 * zmienne wspolrzednaPrzedmiotuY, wspolrzednaPrzedmiotuZ to odpowiednio
	 * wspolrzedne Y i Z obiektu którym manipulujemy
	 */
	private static final long serialVersionUID = 1L;
	private float wymiarCylinderR = 0.1f, wymiarCylinderH = 1.5f;
	private float wymiarCzlonGoraDolX = 0.2f, wymiarCzlonGoraDolY = 0.1f, wymiarCzlonGoraDolZ = 0.4f;
	private float wymiarCzlonWysunWsunX = 0.1f, wymiarCzlonWysunWsunY = 0.04f, wymiarCzlonWysunWsunZ = 0.3f;
	private float maxY = wymiarCylinderH / 2 - wymiarCzlonGoraDolY, minY = -0.55f; // miny
																					// =-31
	private float maxZ = 1.0f, minZ = 0.4f;
	private float wspolrzednaY = 0.0f;
	private float wspolrzednaZ = (2 * wymiarCzlonGoraDolZ) - 0.1f - wymiarCzlonWysunWsunZ;
	private float predkoscPoruszaniaGora = 0.045f,predkoscPoruszaniaDol = 0.045f,  predkoscPoruszaniaWysun = 0.1f, predkoscPoruszaniaWsun = 0.1f;
	private float katUkladu = 0.0f;
	private float katPrzedmiotu = 0.0f;
	private float maxKatWychyleniaUkladu = 90.0f;
	private float predkoscObracaniaUkladu = 5.0f;

	private float wspolrzednaPrzedmiotuY = -0.55f, wspolrzednaPrzedmiotuZ = 1.4f;
	private float odlegloscStalaRobotaOdPrzedmiotu = 0.5f, obecnaOdleglosc = 0.0f;;// potrzebne
																					// do
																					// obliczenia
																					// polozenia
																					// przedmiotu
	private float katPrzedmiotuPodczasSpadania = 0.0f; // potrzebna do zapisania
														// kata calego ukladu,
														// tak by podczas
														// spadania przedmiot
														// nie obracal sie razem
														// z ukladem
	private Transform3D transGoraDol, transWysunWsun, transPrzedmiotu;
	private Transform3D transRotacja;;
	private TransformGroup tg_GoraDol, tg_WysunWsun, tg_Przedmiot;
	private DetektorKolizji kolizjoner;
	private Timer grawitacjaTimer;
	
	private int indeks=0;
	private int indeks2=0;
	private boolean zapis;
	private boolean odczyt=false;
	private float beka;
	private Timer DrogaZadanaTimer;
	private float wsppamiec[]= new float[6];
	private float Odczyt_wsppamiec[] = new float[6];
	private float chech_down_and_up;
	private ArrayList<Float> pamiec_drogi= new ArrayList<Float>();
	private ArrayList<Float> pamiec_drogi1= new ArrayList<Float>();
	private ArrayList<Float> pamiec_drogi2= new ArrayList<Float>();
	private ArrayList<Float> pamiec_drogi3= new ArrayList<Float>();
	private ArrayList<Float> pamiec_drogi4= new ArrayList<Float>();
	private ArrayList<Float> pamiec_drogi5= new ArrayList<Float>();
	
	// Zmienne do dŸwieku
	public static Mixer mixer;
	public static Clip clip;
	

	// TASK ODPOWIEDZIALNY ZA GRAWITACJÊ
	TimerTask grawitacjaTimerTask = new TimerTask() {
		public void run() {
			if (kolizjoner.isInKolizja() == false && wspolrzednaPrzedmiotuY > -0.55f) {
				wspolrzednaPrzedmiotuY = wspolrzednaPrzedmiotuY - 0.004f;
				transPrzedmiotu.set(new Vector3f(0.0f, 0.0f, 0.0f));
				transRotacja.rotY(Math.toRadians(katPrzedmiotuPodczasSpadania));
				transPrzedmiotu.mul(transRotacja);
				transRotacja.set(new Vector3f(0.0f, wspolrzednaPrzedmiotuY, wspolrzednaPrzedmiotuZ));
				transPrzedmiotu.mul(transRotacja);
				tg_Przedmiot.setTransform(transPrzedmiotu);
			}
		}

	};
	TimerTask TimerDrogizadanej = new TimerTask() {
		public void run() {
			if(odczyt && indeks<pamiec_drogi.size())
			{	
				 
			
				Odczyt_wsppamiec[0]=pamiec_drogi.get(indeks2);
				Odczyt_wsppamiec[1]=pamiec_drogi1.get(indeks2);
				Odczyt_wsppamiec[2]=pamiec_drogi2.get(indeks2);
				Odczyt_wsppamiec[3]=pamiec_drogi3.get(indeks2);
				Odczyt_wsppamiec[4]=pamiec_drogi4.get(indeks2);
				Odczyt_wsppamiec[5]=pamiec_drogi5.get(indeks2);
				obecnaOdleglosc = Odczyt_wsppamiec[4] - Odczyt_wsppamiec[1];
				
				
				if (kolizjoner.isInKolizja())  //&& wspolrzednaPrzedmiotuZ >= wspolrzednaZ + wymiarCzlonWysunWsunZ) 
				{
					
					minZ = 0.5f;
					if(chech_down_and_up>Odczyt_wsppamiec[0])
						Odczyt_wsppamiec[3] = Odczyt_wsppamiec[3] + predkoscPoruszaniaDol;
					else if (chech_down_and_up>Odczyt_wsppamiec[0])
						Odczyt_wsppamiec[3] = Odczyt_wsppamiec[3] + predkoscPoruszaniaGora;
						
					
					Odczyt_wsppamiec[4] = odlegloscStalaRobotaOdPrzedmiotu - obecnaOdleglosc + Odczyt_wsppamiec[4];
					transPrzedmiotu.set(new Vector3f(0.0f, 0.0f, 0.0f));
					transRotacja.rotY(Math.toRadians(Odczyt_wsppamiec[2]));
					transPrzedmiotu.mul(transRotacja);
					transRotacja.set(new Vector3f(0.0f, Odczyt_wsppamiec[3], Odczyt_wsppamiec[4]));
					transPrzedmiotu.mul(transRotacja);
					tg_Przedmiot.setTransform(transPrzedmiotu);
					// ----------------------------------------

				} else minZ = 0.4f;
				
				// ---------------------------------------
				// TRANSOFRMACJE CZLONU GORA-DOL
				transGoraDol.set(new Vector3f(0.0f, 0.0f, 0.0f));
				transRotacja.rotY(Math.toRadians(Odczyt_wsppamiec[2]));
				transGoraDol.mul(transRotacja);
				transRotacja.set(new Vector3f(0.0f, Odczyt_wsppamiec[0], wymiarCzlonGoraDolZ - wymiarCylinderR));
				transGoraDol.mul(transRotacja);
				tg_GoraDol.setTransform(transGoraDol);
				// ---------------------------------------

				// TRANSOFRMACJE CZLONU WYSUN-WSUN
				transWysunWsun.set(new Vector3f(0.0f, 0.0f, 0.0f));
				transRotacja.rotY(Math.toRadians(Odczyt_wsppamiec[2]));
				transWysunWsun.rotY(Math.toRadians(Odczyt_wsppamiec[2]));
				transRotacja.set(new Vector3f(0.0f, Odczyt_wsppamiec[0], Odczyt_wsppamiec[1]));
				transWysunWsun.mul(transRotacja);
				tg_WysunWsun.setTransform(transWysunWsun);
				chech_down_and_up=Odczyt_wsppamiec[0];
				
				if(pamiec_drogi.size()==indeks2+1)
				{	
					wspolrzednaY=Odczyt_wsppamiec[0];
					wspolrzednaZ=Odczyt_wsppamiec[1];
					katUkladu=Odczyt_wsppamiec[2];
					wspolrzednaPrzedmiotuY=Odczyt_wsppamiec[3];
					wspolrzednaPrzedmiotuZ=Odczyt_wsppamiec[4];
					katPrzedmiotu=Odczyt_wsppamiec[5];
					odczyt=false;
					indeks2=0;
					katPrzedmiotu = katUkladu;
					kolizjoner.setInKolizja(false);
					katPrzedmiotuPodczasSpadania = katPrzedmiotu;
				}else
					indeks2++;
			}
		}

	};
	// ---------------------------------------
	
	Test_w4() {

		super("Program3D");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();

		// "PLOTNO" miejsce na ktorym bedzie wyswietalny universe
		Canvas3D canvas3D = new Canvas3D(config);
		canvas3D.setPreferredSize(new Dimension(1024, 768));
		// canvas3D.setPreferredSize(new Dimension(1920, 1080));
		canvas3D.addKeyListener(this);
		add(canvas3D);
		pack();
		setVisible(true);
		BranchGroup scena = utworzScene();
		scena.compile();
		// --------------------------

		grawitacjaTimer = new Timer();
		grawitacjaTimer.schedule(grawitacjaTimerTask, 0, 1);
		DrogaZadanaTimer = new Timer();
		DrogaZadanaTimer.schedule(TimerDrogizadanej, 0, 200);
		SimpleUniverse simpleU = new SimpleUniverse(canvas3D);

		// BEHAVIOUR ODPOWIEDZIALNY ZA RUCH KAMER¥
		OrbitBehavior orbit = new OrbitBehavior(canvas3D, OrbitBehavior.STOP_ZOOM);
		orbit.setReverseRotate(true);
		// orbit.setTranslateEnable(false);
		orbit.setSchedulingBounds(new BoundingSphere());
		orbit.setRotYFactor(0);
		orbit.setMinRadius(8.0);
		orbit.setBounds(new BoundingSphere(new Point3d(0.0d, 0.0d, 0.0d), 10d));
		simpleU.getViewingPlatform().setViewPlatformBehavior(orbit);
		// ---------------------------------------

		Transform3D przesuniecie_obserwatora = new Transform3D();
		przesuniecie_obserwatora.set(new Vector3f(0.0f, 0.3f, 10.0f));
		simpleU.getViewingPlatform().getViewPlatformTransform().setTransform(przesuniecie_obserwatora);
		simpleU.addBranchGraph(scena);
		//Dzwiek
Mixer.Info[] mixInfos = AudioSystem.getMixerInfo();
		
		mixer= AudioSystem.getMixer(mixInfos[0]);
		
		DataLine.Info dataInfo = new DataLine.Info(Clip.class, null);
		
		try{clip=(Clip)mixer.getLine(dataInfo);}
		catch(LineUnavailableException lue){lue.printStackTrace();}
		
		try
		{
			URL soundURL = Test_w4.class.getResource("/glos.wav");
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
			clip.open(audioStream);
			
		}catch(LineUnavailableException lue){lue.printStackTrace();}
		catch(UnsupportedAudioFileException uafe){uafe.printStackTrace();}
		catch(IOException ioe){ioe.printStackTrace();}
		do
		{
			try{Thread.sleep(1);}
			catch(InterruptedException ie)
			{
				ie.printStackTrace();
			}
		}while(clip.isActive());
	}

	BranchGroup utworzScene() {

		BranchGroup wezel_scena = new BranchGroup();

		//DODANIE TLA DO SWIATA

		//-------------------------------------
		
		// OBIEKT CYLINDER, BEZ TRANSFORMACJI CZYLI NIERUCHOMY
		Appearance wygladCylindra = new Appearance();
		Material materialCylindra = new Material();
		Texture teksturaCylindra = new TextureLoader("obrazki/cylinderTexture.jpg", this).getTexture();
		wygladCylindra.setTexture(teksturaCylindra);
		Cylinder cylinder = new Cylinder(wymiarCylinderR, wymiarCylinderH, Cylinder.GENERATE_TEXTURE_COORDS,
				wygladCylindra);
		cylinder.setCollidable(false);
		wezel_scena.addChild(cylinder);
		// ---------------------------------------

		// OBIEKT CZLON GORA-DOL
		Appearance wygladCzlonuGoraDol = new Appearance();
		Texture teksturaCzlonuGoraDol = new TextureLoader("obrazki/cylinderTexture.jpg", this).getTexture();
		wygladCzlonuGoraDol.setTexture(teksturaCzlonuGoraDol);
		Box czlonGoraDol = new Box(wymiarCzlonGoraDolX, wymiarCzlonGoraDolY, wymiarCzlonGoraDolZ,
				Box.GENERATE_TEXTURE_COORDS, wygladCzlonuGoraDol);
		czlonGoraDol.setCollidable(false);
		transGoraDol = new Transform3D();
		transGoraDol.set(new Vector3f(0.0f, wspolrzednaY, wymiarCzlonGoraDolZ - wymiarCylinderR));
		transRotacja = new Transform3D();
		tg_GoraDol = new TransformGroup(transGoraDol);
		tg_GoraDol.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		tg_GoraDol.addChild(czlonGoraDol);
		wezel_scena.addChild(tg_GoraDol);
		// ---------------------------------------

		// OBIEKT CZLON WYSUN-WSUN
		Appearance wygladCzlonuWysunWsun = new Appearance();
		Texture teksturaCzlonuWysunWsun = new TextureLoader("obrazki/n2.jpg", this).getTexture();
		wygladCzlonuWysunWsun.setTexture(teksturaCzlonuWysunWsun);
		Box czlonWysunWsun = new Box(
				wymiarCzlonWysunWsunX, wymiarCzlonWysunWsunY, wymiarCzlonWysunWsunZ, Box.GENERATE_TEXTURE_COORDS
						+ Box.ALLOW_PARENT_READ + Box.ALLOW_CHILDREN_READ + Box.ENABLE_COLLISION_REPORTING,
				wygladCzlonuWysunWsun);
		czlonWysunWsun.setUserData("robot");
		transWysunWsun = new Transform3D();
		transWysunWsun.set(new Vector3f(0.0f, wspolrzednaY, wspolrzednaZ));
		tg_WysunWsun = new TransformGroup(transWysunWsun);
		tg_WysunWsun.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		tg_WysunWsun.addChild(czlonWysunWsun);
		wezel_scena.addChild(tg_WysunWsun);
		// ---------------------------------------

		// OBIEKT PODLOGA
		Appearance wygladPodlogi = new Appearance();
		Texture teksturaPodlogi = new TextureLoader("obrazki/floorTexture.jpg", this).getTexture();
		wygladPodlogi.setTexture(teksturaPodlogi);
		Box mojaPodloga = new Box(5f, 0.1f, 5f, Box.GENERATE_TEXTURE_COORDS, wygladPodlogi);
		mojaPodloga.setCollidable(false);
		Transform3D transPodlogi = new Transform3D();
		transPodlogi.set(new Vector3f(0.0f, -0.85f, 0.0f));
		TransformGroup tg_podlogi = new TransformGroup(transPodlogi);
		tg_podlogi.addChild(mojaPodloga);
		wezel_scena.addChild(tg_podlogi);
		// ---------------------------------------

		// PRZEDMIOT KTÓRM MANIPULUJEMY. COLORCUBE BO £ADNE I £ATWE
		//ColorCube przedmiot = new ColorCube(0.2f);
		Appearance wygladS = new Appearance();
		wygladS.setColoringAttributes(new ColoringAttributes(1.0f, 1.0f, 1.0f, 1));
		Sphere przedmiot = new Sphere(0.2f, wygladS);
		transPrzedmiotu = new Transform3D();
		transPrzedmiotu.set(new Vector3f(0.0f, wspolrzednaPrzedmiotuY, wspolrzednaPrzedmiotuZ));
		tg_Przedmiot = new TransformGroup(transPrzedmiotu);
		tg_Przedmiot.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		tg_Przedmiot.addChild(przedmiot);
		wezel_scena.addChild(tg_Przedmiot);
		// ---------------------------------------

		// DODANIE OBIEKTU ODPOWIEDZIALNEGO ZA KOLIZJE. JEST PRZYPISANY DO
		// "PRZEDMIOTU" CZYLI TO CO PODNOSIMU
		kolizjoner = new DetektorKolizji(przedmiot, new BoundingSphere(new Point3d(), 0.2f));
		BoundingSphere boundsKolizjoner = new BoundingSphere(new Point3d(), 100.0);
		kolizjoner.setSchedulingBounds(boundsKolizjoner);
		wezel_scena.addChild(kolizjoner);

		//Dzwiêk
		
		
		// ---------------------------------------
		
		
		return wezel_scena;

	}

	public static void main(String args[]) {
		
		new Test_w4();

	}

	public void kolizjazPodloga() {

		if (kolizjoner.isInKolizja()) {
			if (wspolrzednaPrzedmiotuY - 0.3f > -0.75f) {
				predkoscPoruszaniaDol = 0.045f;
			}
			else
			{
				predkoscPoruszaniaDol = 0.0f;
			}
		}
		else
		{
			predkoscPoruszaniaDol = 0.045f;
		}

	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		//Odtwarzanie dŸwieku
		
		
		// OBSLUGA KLAWIATURY
		if (e.getKeyChar() == 'w' && wspolrzednaY < maxY) {
			wspolrzednaY = wspolrzednaY + predkoscPoruszaniaGora;
			if (kolizjoner.isInKolizja())
				wspolrzednaPrzedmiotuY = wspolrzednaPrzedmiotuY + predkoscPoruszaniaGora;
		} else if (e.getKeyChar() == 's' && wspolrzednaY > minY) {
			kolizjazPodloga();
			wspolrzednaY = wspolrzednaY - predkoscPoruszaniaDol;

			if (kolizjoner.isInKolizja()) {
				wspolrzednaPrzedmiotuY = wspolrzednaPrzedmiotuY - predkoscPoruszaniaDol;
			}
		} else if (e.getKeyChar() == 'a' && wspolrzednaZ > minZ) {
			clip.start(); 
			wspolrzednaZ = wspolrzednaZ - predkoscPoruszaniaWsun;
		} else if (e.getKeyChar() == 'd' && wspolrzednaZ < maxZ) {
		        
			wspolrzednaZ = wspolrzednaZ + predkoscPoruszaniaWysun;
		} else if (e.getKeyChar() == 'q' && katUkladu < maxKatWychyleniaUkladu) {
			clip.start();
		        
			katUkladu = katUkladu + predkoscObracaniaUkladu;
		} else if (e.getKeyChar() == 'e' && katUkladu > -maxKatWychyleniaUkladu) {
		      
			katUkladu = katUkladu - predkoscObracaniaUkladu;
		} else if (e.getKeyChar() == 'n') {
			katPrzedmiotu = katUkladu;
			kolizjoner.setInKolizja(false);
			katPrzedmiotuPodczasSpadania = katPrzedmiotu;
		}
		obecnaOdleglosc = wspolrzednaPrzedmiotuZ - wspolrzednaZ;
		if(e.getKeyChar()=='p')//rozpoczynamy zapis trajektorii
		{
			zapis=true;
		}
		if(e.getKeyChar()=='k') //koñczymy zapisywanie
			zapis=false;
		if(e.getKeyChar()=='o')//odczytujemy zapisan¹ trajektorie
		{
			odczyt=true;
			kolizjoner.setInKolizja(false);
			float a= pamiec_drogi3.get(0);
			float b=pamiec_drogi4.get(0);
			float c=pamiec_drogi5.get(0);
			wsppamiec[0]=wspolrzednaY;
			wsppamiec[1]=wspolrzednaZ;
			float d=pamiec_drogi2.get(0);
			b= odlegloscStalaRobotaOdPrzedmiotu - obecnaOdleglosc + b;
			transPrzedmiotu.set(new Vector3f(0.0f, 0.0f, 0.0f));
			transRotacja.rotY(Math.toRadians(d));
			transPrzedmiotu.mul(transRotacja);
			transRotacja.set(new Vector3f(0.0f, a, b));
			transPrzedmiotu.mul(transRotacja);
			tg_Przedmiot.setTransform(transPrzedmiotu);
		}
		if(e.getKeyChar()=='t' && odczyt)//przerywamy odczytywanie trajektorii
		{
			odczyt=false;
			indeks2=0;
		}
		if(e.getKeyChar()=='v'&& odczyt)//przerywamy odczytywanie trajektorii+przdmiot jest upuszczany
		{
			odczyt=false;
			indeks2=0;
			katPrzedmiotu = katUkladu;
			kolizjoner.setInKolizja(false);
			katPrzedmiotuPodczasSpadania = katPrzedmiotu;
		}
		if(e.getKeyChar()=='b')//usuwanie trakejktorii
		{
			for(int x=0;x<pamiec_drogi.size();x=0)
			{
				pamiec_drogi.remove(x);
				pamiec_drogi1.remove(x);
				pamiec_drogi2.remove(x);
				pamiec_drogi3.remove(x);
				pamiec_drogi4.remove(x);
				pamiec_drogi5.remove(x);
			}
			
		}
		if(zapis)
			{	wsppamiec[0]=wspolrzednaY;
				wsppamiec[1]=wspolrzednaZ;
				wsppamiec[2]=katUkladu;
				wsppamiec[3]=wspolrzednaPrzedmiotuY;
				wsppamiec[4]=wspolrzednaPrzedmiotuZ;
				wsppamiec[5]=katPrzedmiotu;
				
				pamiec_drogi.add(wsppamiec[0]);
				pamiec_drogi1.add(wsppamiec[1]);
				pamiec_drogi2.add(wsppamiec[2]);
				pamiec_drogi3.add(wsppamiec[3]);
				pamiec_drogi4.add(wsppamiec[4]);
				pamiec_drogi5.add(wsppamiec[5]);
			    
			}
		

		if (kolizjoner.isInKolizja())//&& wspolrzednaPrzedmiotuZ >= wspolrzednaZ + wymiarCzlonWysunWsunZ) {
		{

			minZ = 0.5f;
			
				
			wspolrzednaPrzedmiotuZ = odlegloscStalaRobotaOdPrzedmiotu - obecnaOdleglosc + wspolrzednaPrzedmiotuZ;
			transPrzedmiotu.set(new Vector3f(0.0f, 0.0f, 0.0f));
			transRotacja.rotY(Math.toRadians(katUkladu));
			transPrzedmiotu.mul(transRotacja);
			transRotacja.set(new Vector3f(0.0f, wspolrzednaPrzedmiotuY, wspolrzednaPrzedmiotuZ));
			transPrzedmiotu.mul(transRotacja);
			tg_Przedmiot.setTransform(transPrzedmiotu);
			// ----------------------------------------

		} else minZ = 0.4f;
		
		// ---------------------------------------

		// TRANSOFRMACJE CZLONU GORA-DOL
		transGoraDol.set(new Vector3f(0.0f, 0.0f, 0.0f));
		transRotacja.rotY(Math.toRadians(katUkladu));
		transGoraDol.mul(transRotacja);
		transRotacja.set(new Vector3f(0.0f, wspolrzednaY, wymiarCzlonGoraDolZ - wymiarCylinderR));
		transGoraDol.mul(transRotacja);
		tg_GoraDol.setTransform(transGoraDol);
		// ---------------------------------------

		// TRANSOFRMACJE CZLONU WYSUN-WSUN
		transWysunWsun.set(new Vector3f(0.0f, 0.0f, 0.0f));
		transRotacja.rotY(Math.toRadians(katUkladu));
		transWysunWsun.rotY(Math.toRadians(katUkladu));
		transRotacja.set(new Vector3f(0.0f, wspolrzednaY, wspolrzednaZ));
		transWysunWsun.mul(transRotacja);
		tg_WysunWsun.setTransform(transWysunWsun);
		// ---------------------------------------
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}
	
}
