package cl.robermotos.delivery.adapter;
import cl.robermotos.delivery.dto.*;
public interface DeliveryProviderAdapter { String provider(); String displayName(); DeliveryQuoteDTO quote(DeliveryRequest request); DispatchResult dispatch(String orderId,DeliveryRequest request,String quoteId); }