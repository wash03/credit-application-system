package me.dio.credit.application.system.service.impl

import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.exception.BusinessException
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.service.ICreditService
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.time.LocalDate
import java.util.*

@Service
class CreditService(
  private val creditRepository: CreditRepository,
  private val customerService: CustomerService
) : ICreditService {
  override fun save(credit: Credit): Credit {
    this.validDayFirstInstallment(credit.dayFirstInstallment)

    if (credit.numberOfInstallments > 48) {
      throw BusinessException("Número de parcelas excede o limite máximo de 48")
    }

    credit.apply {
      customer = customerService.findById(credit.customer?.id!!)
    }
    return this.creditRepository.save(credit)
  }

  override fun findAllByCustomer(customerId: Long): List<Credit> =
    this.creditRepository.findAllByCustomerId(customerId)

  override fun findByCreditCode(customerId: Long, creditCode: UUID): Credit {
    val credit: Credit = (this.creditRepository.findByCreditCode(creditCode)
      ?: throw BusinessException("Creditcode $creditCode not found"))
    return if (credit.customer?.id == customerId) credit
    else throw IllegalArgumentException("Contact admin")
    /*if (credit.customer?.id == customerId) {
      return credit
    } else {
      throw RuntimeException("Contact admin")
    }*/
  }

  private fun validDayFirstInstallment(dayFirstInstallment: LocalDate) {
    if (dayFirstInstallment.isBefore(LocalDate.now().plusMonths(3))) {
      throw BusinessException("A data da primeira parcela deve ser no máximo 3 meses após o dia atual")
    }
  }
}

