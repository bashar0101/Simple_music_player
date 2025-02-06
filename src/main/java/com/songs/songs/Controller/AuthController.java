package com.songs.songs.Controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import com.songs.songs.Entity.Song;
import com.songs.songs.Entity.User;
import com.songs.songs.Repo.SongRepository;
import com.songs.songs.Repo.UserRepository;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SongRepository songRepository;

    @GetMapping("/home")
    public String showHome(Model model, Authentication authentication) {
        // Reload the user from the database to get updated songs
        User currentUser = (User) authentication.getPrincipal();
        User freshUser = userRepository.findById(currentUser.getId()).orElseThrow();

        model.addAttribute("songs", freshUser.getSongs());
        model.addAttribute("newSong", new Song());
        return "home";

    }

    @PostMapping("/upload")
    public String uploadSong(@ModelAttribute Song newSong,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {

        User user = (User) authentication.getPrincipal();

        // Create upload directory inside the project's static folder
        String uploadDir = "src/main/resources/static/uploads/";
        File directory = new File(uploadDir);
        if (!directory.exists())
            directory.mkdirs();

        // Save file
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Save song metadata with correct path
        newSong.setFilePath("/uploads/" + fileName); // Notice the path change
        newSong.setUser(user);
        songRepository.save(newSong);

        return "redirect:/home?nocache=" + System.currentTimeMillis(); // Bust browser cache

    }

    @GetMapping("/signup")
    public String showSignUpForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/register")
    public String registerUser(@Valid User user, BindingResult result, Model model) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            result.rejectValue("username", "error.user", "Username already taken");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            result.rejectValue("email", "error.user", "Email already registered");
        }

        if (result.hasErrors()) {
            return "signup";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    // @GetMapping("/home")
    // public String showHome() {
    // return "home";
    // }

}