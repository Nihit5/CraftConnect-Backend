package com.nihit.craft_connect.controller.shipping;

import com.nihit.craft_connect.config.CustomMessageSource;
import com.nihit.craft_connect.constants.SuccessConstants;
import com.nihit.craft_connect.controller.BaseController;
import com.nihit.craft_connect.dto.GlobalApiResponse;
import com.nihit.craft_connect.dto.shippingAddress.ShippingAddressRequestPojo;
import com.nihit.craft_connect.service.shipping.ShippingAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/address")
@RequiredArgsConstructor
public class ShippingAddressController extends BaseController {

    private final ShippingAddressService shippingAddressService;
    private final CustomMessageSource customMessageSource;

    @PostMapping("/add")
    public ResponseEntity<GlobalApiResponse> addAddress(@RequestBody ShippingAddressRequestPojo request) {
        return ResponseEntity.ok(
                successResponse(
                        customMessageSource.get(SuccessConstants.SUCCESS_CREATE, "Address"),
                        shippingAddressService.addAddress(request)
                )
        );
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<GlobalApiResponse> updateAddress(
            @PathVariable Long addressId,
            @RequestBody ShippingAddressRequestPojo request) {
        return ResponseEntity.ok(
                successResponse(
                        customMessageSource.get(SuccessConstants.SUCCESS_UPDATE, "Address"),
                        shippingAddressService.updateAddress(addressId, request)
                )
        );
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<GlobalApiResponse> deleteAddress(@PathVariable Long addressId) {
        shippingAddressService.deleteAddress(addressId);
        return ResponseEntity.ok(
                successResponse(
                        customMessageSource.get(SuccessConstants.SUCCESS_DELETE, "Address"),
                        null
                )
        );
    }

    @GetMapping("/my-addresses")
    public ResponseEntity<GlobalApiResponse> getMyAddresses() {
        return ResponseEntity.ok(
                successResponse(
                        customMessageSource.get(SuccessConstants.SUCCESS_RETRIEVE, "Addresses"),
                        shippingAddressService.getMyAddresses()
                )
        );
    }

    @PutMapping("/{addressId}/default")
    public ResponseEntity<GlobalApiResponse> setDefaultAddress(@PathVariable Long addressId) {
        shippingAddressService.setDefaultAddress(addressId);
        return ResponseEntity.ok(
                successResponse(
                        customMessageSource.get(SuccessConstants.SUCCESS_UPDATE, "Default address"),
                        null
                )
        );
    }
}
