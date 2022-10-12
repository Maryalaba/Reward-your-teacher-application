package com.decagon.rewardyourteacherapi11bjavapodf2.serviceImpl;

import com.decagon.rewardyourteacherapi11bjavapodf2.dto.*;
import com.decagon.rewardyourteacherapi11bjavapodf2.enums.Role;
import com.decagon.rewardyourteacherapi11bjavapodf2.exceptions.OAuth2AuthenticationException;
import com.decagon.rewardyourteacherapi11bjavapodf2.exceptions.UserAlreadyExistException;
import com.decagon.rewardyourteacherapi11bjavapodf2.exceptions.UserNotFoundException;
import com.decagon.rewardyourteacherapi11bjavapodf2.model.Subject;
import com.decagon.rewardyourteacherapi11bjavapodf2.model.Teacher;
import com.decagon.rewardyourteacherapi11bjavapodf2.model.User;
import com.decagon.rewardyourteacherapi11bjavapodf2.model.Wallet;
import com.decagon.rewardyourteacherapi11bjavapodf2.repository.SubjectRepository;
import com.decagon.rewardyourteacherapi11bjavapodf2.repository.UserRepository;
import com.decagon.rewardyourteacherapi11bjavapodf2.repository.WalletRepository;
import com.decagon.rewardyourteacherapi11bjavapodf2.response.ApiResponse;
import com.decagon.rewardyourteacherapi11bjavapodf2.response.UserRegistrationResponse;
import com.decagon.rewardyourteacherapi11bjavapodf2.security.JwtUtil;
import com.decagon.rewardyourteacherapi11bjavapodf2.service.AuthService;
import com.decagon.rewardyourteacherapi11bjavapodf2.utils.CloudUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;


@RequiredArgsConstructor
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final SubjectRepository subjectRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    private final AuthenticationManager authenticationManager;
    private final WalletRepository walletRepository;

    private final CloudUtils cloudUtils;


    @Override
    public UserRegistrationResponse registerUser(UserDto userDto) {
        String email = userDto.getEmail();
        Optional<User> existingUser = userRepository.findUserByEmail(email);
        if(existingUser.isEmpty()){
            User user = new User();
            user.setName(userDto.getName());
            user.setEmail(userDto.getEmail());
            user.setTelephone(userDto.getTelephone());
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            user.setSchool(userDto.getSchool());
            user.setRole(Role.STUDENT);
            userRepository.save(user);
            walletRepository.save( new Wallet(new BigDecimal("0"), user));
            return new UserRegistrationResponse("success", LocalDateTime.now());
        }else {
            throw new UserAlreadyExistException("User already exist");
        }
    }

    @Override
    public UserRegistrationResponse registerTeacher(TeacherRegistrationDto teacherDto, MultipartFile validId) throws IOException {
        String email = teacherDto.getEmail();
        Optional<User> existingUser = userRepository.findUserByEmail(email);

        if(existingUser.isEmpty()){
            Teacher teacher = new Teacher();
            teacher.setName(teacherDto.getName());
            teacher.setEmail(teacherDto.getEmail());
            teacher.setPassword(passwordEncoder.encode(teacherDto.getPassword()));
            teacher.setTelephone(teacherDto.getTelephone());
            teacher.setSchool(teacherDto.getSchool());
            teacher.setYearsOfService(teacherDto.getYearsOfService());
            teacher.setSchoolType(teacherDto.getSchoolType());
            teacher.setRole(Role.TEACHER);
            teacher.setValidId(cloudUtils.uploadImage(validId));
            userRepository.save(teacher);
            walletRepository.save( new Wallet(new BigDecimal("0"), teacher));
            teacherDto.getSubjectList().forEach(subject -> {
                subjectRepository.save(new Subject(subject , teacher));
            });

            return new UserRegistrationResponse("success", LocalDateTime.now());
        }else{
            throw new UserAlreadyExistException("User already exist");
        }
    }

    @Override
    public ApiResponse<PrincipalDto> login(LoginDTO loginDTO) {
        Authentication authenticate;
        try {
            authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())
            );
        } catch (AuthenticationException ex) {
            log.error(ex.getMessage());
            throw new UserNotFoundException("invalid username or password");
        }
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        User loggedInUser = userRepository.findUserByEmail(loginDTO.getEmail()).get();
        return new ApiResponse<>("success" , LocalDateTime.now() , new PrincipalDto( loggedInUser.getId() , loggedInUser.getName() ,  loggedInUser.getEmail(), loggedInUser.getRole(), jwtUtil.generateToken(loginDTO.getEmail()), loggedInUser.getSchool()));

    }
    @Override
    public ApiResponse<PrincipalDto> authenticateOAuth2User(OAuth2UserInfo auth2UserInfo) {
        User user = userRepository.findUserByEmail(auth2UserInfo.getEmail()).orElseThrow(() ->
                new OAuth2AuthenticationException("Email not Found " + auth2UserInfo.getEmail())); //TODO
        String token = "Bearer " + jwtUtil.generateToken(auth2UserInfo.getEmail());
        return new ApiResponse<>("success", LocalDateTime.now(), new PrincipalDto(user.getId(), user.getName(), user.getEmail(), user.getRole(), token, user.getSchool()));
    }


}
