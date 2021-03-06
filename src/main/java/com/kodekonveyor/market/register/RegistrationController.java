package com.kodekonveyor.market.register;

import java.util.HashSet;
import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kodekonveyor.authentication.AuthenticatedUserService;
import com.kodekonveyor.authentication.UserEntity;
import com.kodekonveyor.logging.LoggingMarkerConstants;
import com.kodekonveyor.market.MarketConstants;
import com.kodekonveyor.market.UrlMapConstants;
import com.kodekonveyor.market.ValidationException;
import com.kodekonveyor.market.payment.LegalFormEntity;
import com.kodekonveyor.market.payment.LegalFormEntityRepository;

@RestController
public class RegistrationController {

  @Autowired
  AuthenticatedUserService authenticatedUserService;

  @Autowired
  MarketUserEntityRepository marketUserEntityRepository;

  @Autowired
  LegalFormEntityRepository legalFormEntityRepository;

  @Autowired
  Logger logger;

  @PostMapping(UrlMapConstants.REGISTER_USER_PATH)
  public MarketUserDTO
      call(final @RequestBody MarketUserDTO marketUserDTO) {
    logger.info(
        LoggingMarkerConstants.REGISTER, marketUserDTO.toString()
    );
    doStore(marketUserDTO);

    logger.debug(
        LoggingMarkerConstants.REGISTER,
        MarketConstants.MARKET_USER_RETURNED_SUCCESSFULLY +
            marketUserDTO.getId()
    );
    return marketUserDTO;
  }

  private void doStore(final MarketUserDTO marketUserDTO) {

    final Optional<LegalFormEntity> legalform =
        legalFormEntityRepository.findById(marketUserDTO.getLegalForm());
    if (legalform.isEmpty())
      throw new ValidationException(RegisterConstants.NO_SUCH_LEGAL_FORM);
    final UserEntity userEntity = authenticatedUserService.call();

    final MarketUserEntity entity =
        createMarketUserEntity(marketUserDTO, legalform, userEntity);

    marketUserEntityRepository.save(entity);

  }

  private MarketUserEntity createMarketUserEntity(
      final MarketUserDTO marketUserDTO,
      final Optional<LegalFormEntity> legalForm, final UserEntity userEntity
  ) {
    final MarketUserEntity entity = new MarketUserEntity();
    entity.setBalanceInCents(0L);
    entity.setIsTermsAccepted(marketUserDTO.getIsTermsAccepted());
    entity.setEmail(marketUserDTO.getEmail());
    entity.setLegalAddress(marketUserDTO.getLegalAddress());
    entity.setLegalName(marketUserDTO.getLegalName());
    entity.setPersonalName(marketUserDTO.getPersonalName());
    entity.setUser(userEntity);
    entity.setLegalForm(legalForm.get());
    entity.setPaymentDetail(new HashSet<>());
    return entity;
  }

}
