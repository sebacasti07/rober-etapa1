package cl.robermotos.delivery.adapter;
import org.springframework.stereotype.Service;
@Service public class RappiCargoAdapter extends AbstractMockAdapter { public String provider(){return "RAPPI";} public String displayName(){return "Rappi Cargo";} protected long base(){return 2100;} protected long perKm(){return 350;} protected int etaBase(){return 32;} }