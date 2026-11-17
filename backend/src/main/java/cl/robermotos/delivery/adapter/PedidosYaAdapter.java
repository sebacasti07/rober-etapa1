package cl.robermotos.delivery.adapter;
import org.springframework.stereotype.Service;
@Service public class PedidosYaAdapter extends AbstractMockAdapter { public String provider(){return "PEDIDOSYA";} public String displayName(){return "PedidosYa Envíos";} protected long base(){return 1800;} protected long perKm(){return 380;} protected int etaBase(){return 35;} }