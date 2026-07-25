package com.nihit.craft_connect.service.shipping;


import com.nihit.craft_connect.dto.shippingAddress.ShippingAddressRequestPojo;
import com.nihit.craft_connect.dto.shippingAddress.ShippingAddressResponsePojo;

import java.util.List;

public interface ShippingAddressService {
    ShippingAddressResponsePojo addAddress(ShippingAddressRequestPojo request);
    ShippingAddressResponsePojo updateAddress(Long addressId, ShippingAddressRequestPojo request);
    void deleteAddress(Long addressId);
    List<ShippingAddressResponsePojo> getMyAddresses();
    void setDefaultAddress(Long addressId);
}
