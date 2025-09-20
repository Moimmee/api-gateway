package com.moimmee.apigateway.infra.security.jwt.filter

import com.moimmee.apigateway.infra.security.jwt.extrator.JwtExtractor
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtExtractor: JwtExtractor,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = extractToken(request)

        if (token == null || !jwtExtractor.validateToken(token)) {
            return filterChain.doFilter(request, response)
        }

        val userId = jwtExtractor.getUserId(token)
        val role = jwtExtractor.getRole(token)

        val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))
        val authentication = UsernamePasswordAuthenticationToken(userId, null, authorities)

        SecurityContextHolder.getContext().authentication = authentication
    }

    private fun extractToken(httpRequest: HttpServletRequest): String? {
        val authHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION)

        return if (authHeader?.startsWith("Bearer ") == true) authHeader.substring(7)
        else null
    }
}