package com.nihit.craft_connect.service.shipping.impl;

import com.nihit.craft_connect.config.CustomMessageSource;
import com.nihit.craft_connect.config.UserDetailConfig;
import com.nihit.craft_connect.constants.StringConstants;
import com.nihit.craft_connect.dto.shippingAddress.ShippingAddressRequestPojo;
import com.nihit.craft_connect.dto.shippingAddress.ShippingAddressResponsePojo;
import com.nihit.craft_connect.entity.ShippingAddress;
import com.nihit.craft_connect.entity.User;
import com.nihit.craft_connect.enums.AddressType;
import com.nihit.craft_connect.exception.AppException;
import com.nihit.craft_connect.repository.ShippingAddressRepository;
import com.nihit.craft_connect.repository.UserRepository;
import com.nihit.craft_connect.service.shipping.ShippingAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingAddressServiceImpl implements ShippingAddressService {

    private final ShippingAddressRepository shippingAddressRepository;
    private final UserRepository userRepository;
    private final UserDetailConfig userDetailConfig;
    private final CustomMessageSource customMessageSource;

    @Override
    @Transactional
    public ShippingAddressResponsePojo addAddress(ShippingAddressRequestPojo request) {
        Long userId = userDetailConfig.getLoggedInUserId();
        validateRequest(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(
                        customMessageSource.get(StringConstants.NOT_FOUND, "User")));

        List<ShippingAddress> existing = shippingAddressRepository.findByUserId(userId);

        ShippingAddress addr = new ShippingAddress();
        addr.setUser(user);
        applyRequest(addr, request);
        addr.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        addr.setModifiedDate(new Timestamp(System.currentTimeMillis()));

        // first address a user adds is automatically their default
        boolean makeDefault = existing.isEmpty() || Boolean.TRUE.equals(request.getIsDefault());
        addr.setIsDefault(makeDefault);

        if (makeDefault && !existing.isEmpty()) {
            unsetOtherDefaults(existing);
        }

        ShippingAddress saved = shippingAddressRepository.save(addr);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ShippingAddressResponsePojo updateAddress(Long addressId, ShippingAddressRequestPojo request) {
        Long userId = userDetailConfig.getLoggedInUserId();
        validateRequest(request);

        ShippingAddress addr = shippingAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new AppException(
                        customMessageSource.get(StringConstants.NOT_FOUND, "Address")));

        applyRequest(addr, request);
        addr.setModifiedDate(new Timestamp(System.currentTimeMillis()));

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            unsetOtherDefaults(shippingAddressRepository.findByUserId(userId));
            addr.setIsDefault(true);
        }

        ShippingAddress saved = shippingAddressRepository.save(addr);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteAddress(Long addressId) {
        Long userId = userDetailConfig.getLoggedInUserId();
        ShippingAddress addr = shippingAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new AppException(
                        customMessageSource.get(StringConstants.NOT_FOUND, "Address")));

        boolean wasDefault = Boolean.TRUE.equals(addr.getIsDefault());
        shippingAddressRepository.delete(addr);

        // promote another address to default so checkout always has one to preselect
        if (wasDefault) {
            List<ShippingAddress> remaining = shippingAddressRepository.findByUserId(userId);
            if (!remaining.isEmpty()) {
                ShippingAddress next = remaining.get(0);
                next.setIsDefault(true);
                shippingAddressRepository.save(next);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShippingAddressResponsePojo> getMyAddresses() {
        Long userId = userDetailConfig.getLoggedInUserId();
        return shippingAddressRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long addressId) {
        Long userId = userDetailConfig.getLoggedInUserId();
        ShippingAddress addr = shippingAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new AppException(
                        customMessageSource.get(StringConstants.NOT_FOUND, "Address")));

        unsetOtherDefaults(shippingAddressRepository.findByUserId(userId));
        addr.setIsDefault(true);
        shippingAddressRepository.save(addr);
    }

    private void validateRequest(ShippingAddressRequestPojo request) {
        if (request.getAddressType() == AddressType.OTHER
                && (request.getAddressLabel() == null || request.getAddressLabel().isBlank())) {
            throw new AppException("Please provide a label for this address type.");
        }
    }

    private void applyRequest(ShippingAddress addr, ShippingAddressRequestPojo request) {
        addr.setRecipientName(request.getRecipientName());
        addr.setMobileNumber(request.getMobileNumber());
        addr.setProvince(request.getProvince());
        addr.setDistrict(request.getDistrict());
        addr.setAddress(request.getAddress());
        addr.setLandmark(request.getLandmark());
        addr.setAddressType(request.getAddressType());
        addr.setAddressLabel(request.getAddressType() == AddressType.OTHER ? request.getAddressLabel() : null);
    }

    private void unsetOtherDefaults(List<ShippingAddress> addresses) {
        for (ShippingAddress a : addresses) {
            if (Boolean.TRUE.equals(a.getIsDefault())) {
                a.setIsDefault(false);
                shippingAddressRepository.save(a);
            }
        }
    }

    private ShippingAddressResponsePojo mapToResponse(ShippingAddress addr) {
        ShippingAddressResponsePojo pojo = new ShippingAddressResponsePojo();
        pojo.setId(addr.getId());
        pojo.setRecipientName(addr.getRecipientName());
        pojo.setMobileNumber(addr.getMobileNumber());
        pojo.setProvince(addr.getProvince());
        pojo.setDistrict(addr.getDistrict());
        pojo.setAddress(addr.getAddress());
        pojo.setLandmark(addr.getLandmark());
        pojo.setAddressType(addr.getAddressType());
        pojo.setAddressLabel(addr.getAddressLabel());
        pojo.setIsDefault(addr.getIsDefault());
        return pojo;
    }
}