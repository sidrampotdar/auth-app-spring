package com.example.spring_auth_app.spring_auth_app.security;

import com.example.spring_auth_app.spring_auth_app.repositories.UserRepository;
import com.example.spring_auth_app.spring_auth_app.utilities.UserHelper;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
        private final  JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if(header!=null && header.startsWith("Bearer ")) {
            // extract and validate token
            String token = header.substring(7);// after Bearer
            if(!jwtService.isAccessToken(token)){
               filterChain.doFilter(request,response);
                return;
            }

            try{
               Jws<Claims> parse   =  jwtService.parse(token);

                Claims payload = parse.getPayload();



                String userId = payload.getSubject();
                UUID uuid = UserHelper.parseUUID(userId);
                userRepository.findById(uuid).ifPresent(user->{
                    if(!user.isEnable()){
                        //check for user enable or not
                        try {
                            filterChain.doFilter(request,response);
                        } catch (IOException | ServletException e) {
                            throw new RuntimeException(e);
                        }
                    }
                  List<GrantedAuthority> authorities =  user.getRoles()==null?List.of():user.getRoles().stream().map(role->new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            user.getEmail(),
                            null,
                            authorities
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                  if(SecurityContextHolder.getContext().getAuthentication()==null)
                     SecurityContextHolder.getContext().setAuthentication(authentication);

                });


            }
            catch(Exception e){
                e.printStackTrace();
            }
            // set in security context
        }
        filterChain.doFilter(request, response);
    }
}
