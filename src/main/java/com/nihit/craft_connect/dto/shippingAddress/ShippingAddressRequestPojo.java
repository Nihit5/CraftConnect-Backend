package com.nihit.craft_connect.dto.shippingAddress;

import com.nihit.craft_connect.enums.AddressType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShippingAddressRequestPojo {
    private String recipientName;
    private String mobileNumber;
    private String province;
    private String district;
    private String address;
    private String landmark;
    private AddressType addressType;
    private String addressLabel;
    private Boolean isDefault;
}
