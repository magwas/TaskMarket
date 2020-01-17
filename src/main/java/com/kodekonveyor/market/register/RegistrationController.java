package com.kodekonveyor.market.register;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import com.kodekonveyor.authentication.AuthenticatedUserService;
import com.kodekonveyor.authentication.UserEntity;

public class RegistrationController {

  @Autowired
  private AuthenticatedUserService authenticatedUserService;

  @Autowired
  private MarketUserEntityRepository marketUserEntityRepository;

  public void
      call(final @RequestBody RegistrationInfoDTO registrationInfoDTO) {
    doStore(registrationInfoDTO);

  }

  private void doStore(final RegistrationInfoDTO registrationInfoDTO) {
    final UserEntity userEntity = authenticatedUserService.call();

    MarketUserEntity entity;

    entity = new MarketUserEntity();
    final UserLegalInfoEntity legal = new UserLegalInfoEntity();
    legal.setCountry(registrationInfoDTO.getCountry());
    legal.setEmail(registrationInfoDTO.getEmail());
    legal.setLegalAddress(registrationInfoDTO.getLegalAddress());
    legal.setLegalName(registrationInfoDTO.getLegalName());
    legal.setPaymentDetails(registrationInfoDTO.getPaymentDetails());
    legal.setPaymentRegime(registrationInfoDTO.getPaymentRegime());

    entity.setLegal(legal);
    entity.setLogin(userEntity);

    marketUserEntityRepository.save(entity);

  }

}