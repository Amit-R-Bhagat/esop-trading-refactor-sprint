package com.esop.controller


import com.esop.HttpException
import com.esop.dto.AddInventoryDTO
import com.esop.dto.AddWalletDTO
import com.esop.dto.UserCreationDTO
import com.esop.exceptions.UserNotFoundException
import com.esop.service.*
import com.fasterxml.jackson.core.JsonProcessingException
import io.micronaut.core.convert.exceptions.ConversionErrorException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import io.micronaut.web.router.exceptions.UnsatisfiedBodyRouteException
import jakarta.inject.Inject
import javax.validation.ConstraintViolationException
import javax.validation.Valid


@Validated
@Controller("/user")
class UserController {

    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var orderService: OrderService

    @Error(exception = HttpException::class, global = true)
    fun onHttpException(exception: HttpException): HttpResponse<*> {
        return HttpResponse.status<Map<String, ArrayList<String>>>(exception.status)
            .body(mapOf("errors" to arrayListOf(exception.message)))
    }

    @Error(exception = JsonProcessingException::class, global = true)
    fun onJSONProcessingExceptionError(ex: JsonProcessingException): HttpResponse<Map<String, ArrayList<String>>> {
        return HttpResponse.badRequest(mapOf("errors" to arrayListOf("Invalid JSON format")))
    }

    @Error(exception = UnsatisfiedBodyRouteException::class, global = true)
    fun onUnsatisfiedBodyRouteException(
        request: HttpRequest<*>,
        ex: UnsatisfiedBodyRouteException
    ): HttpResponse<Map<String, List<*>>> {
        return HttpResponse.badRequest(mapOf("errors" to arrayListOf("Request body missing")))
    }

    @Error(status = HttpStatus.NOT_FOUND, global = true)
    fun onRouteNotFound(): HttpResponse<Map<String, List<*>>> {
        return HttpResponse.notFound(mapOf("errors" to arrayListOf("Route not found")))
    }

    @Error(exception = ConversionErrorException::class, global = true)
    fun onConversionErrorException(ex: ConversionErrorException): HttpResponse<Map<String, List<*>>> {
        return HttpResponse.badRequest(mapOf("errors" to arrayListOf(ex.message)))
    }

    @Error(exception = ConstraintViolationException::class, global = true)
    fun onConstraintViolationException(ex: ConstraintViolationException): HttpResponse<Map<String, List<*>>> {
        return HttpResponse.badRequest(mapOf("errors" to ex.constraintViolations.map { it.message }))
    }

    @Error(exception = RuntimeException::class, global = true)
    fun onRuntimeError(ex: RuntimeException): HttpResponse<Map<String, List<*>>> {
        return HttpResponse.serverError(mapOf("errors" to arrayListOf(ex.message)))
    }

    @Error(exception = UserNotFoundException::class, global = true)
    fun onUserNotFoundException(ex: UserNotFoundException): HttpResponse<Map<String, List<String>>> {
        return HttpResponse.notFound(mapOf("errors" to arrayListOf("User not found")))
    }

    @Post(uri = "/register", consumes = [MediaType.APPLICATION_JSON], produces = [MediaType.APPLICATION_JSON])
    fun register(@Body @Valid userData: UserCreationDTO): HttpResponse<*> {
        val newUser = this.userService.registerUser(userData)
        if (newUser["error"] != null) {
            return HttpResponse.badRequest(newUser)
        }
        return HttpResponse.ok(newUser)
    }

    @Get(uri = "/{userName}/accountInformation", produces = [MediaType.APPLICATION_JSON])
    fun getAccountInformation(userName: String): HttpResponse<*> {
        val userData = this.userService.accountInformation(userName)

        if (userData["error"] != null) {
            return HttpResponse.badRequest(userData)
        }

        return HttpResponse.ok(userData)
    }

    @Post(
        uri = "{userName}/inventory",
        consumes = [MediaType.APPLICATION_JSON],
        produces = [MediaType.APPLICATION_JSON]
    )
    fun addInventory(userName: String, @Body @Valid body: AddInventoryDTO): HttpResponse<*> {
        val newInventory = this.userService.addingInventory(body, userName)

        if (newInventory["error"] != null) {
            return HttpResponse.badRequest(newInventory)
        }
        return HttpResponse.ok(newInventory)
    }


    @Post(uri = "{userName}/wallet", consumes = [MediaType.APPLICATION_JSON], produces = [MediaType.APPLICATION_JSON])
    fun addWallet(userName: String, @Body @Valid body: AddWalletDTO): HttpResponse<*> {
        val addedMoney = this.userService.addingMoney(body, userName)

        if (addedMoney["error"] != null) {
            return HttpResponse.badRequest(addedMoney)
        }
        return HttpResponse.ok(addedMoney)

    }


}