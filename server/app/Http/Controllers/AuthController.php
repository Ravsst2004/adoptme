<?php

namespace App\Http\Controllers;

use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Log;
use Illuminate\Validation\ValidationException;

class AuthController extends Controller
{
    public function register(Request $request)
    {
        try {
            $validated = $request->validate([
                'name' => 'required|string|max:255',
                'email' => 'required|email|unique:users,email',
                'password' => 'required|string|min:6',
            ]);

            $user = User::create([
                'name' => $validated['name'],
                'email' => $validated['email'],
                'password' => Hash::make($validated['password']),
            ]);

            Log::info('User created successfully', ['user' => $user]);

            return response()->json([
                'status' => true,
                'message' => 'User registered successfully',
                'data' => $user,
            ], 201);
        } catch (ValidationException $e) {
            $errors = $e->errors();

            return response()->json([
                'status' => false,
                'message' => 'Validation error',
                'errors' => $errors,
            ], 422);
        } catch (\Exception $e) {
            Log::error('Error creating user', ['error' => $e->getMessage()]);

            return response()->json([
                'status' => false,
                'message' => 'Internal Server Error',
            ], 500);
        }
    }

    public function login(Request $request)
    {
        try {
            $validated = $request->validate([
                'email' => 'required|email',
                'password' => 'required|string',
            ]);

            $user = User::where('email', $validated['email'])->first();

            if (!$user || !Hash::check($validated['password'], $user->password)) {
                return response()->json([
                    'status' => false,
                    'message' => 'Invalid Credentials',
                ], 401);
            }

            $token = $user->createToken('auth_token')->plainTextToken;

            return response()->json([
                'status' => true,
                'message' => 'Login successful',
                'data' => [
                    'user' => $user,
                    'token' => $token,
                ],
            ]);
        } catch (ValidationException $e) {
            $errors = $e->errors();

            return response()->json([
                'status' => false,
                'message' => 'Validation error',
                'errors' => $errors,
            ], 422);
        } catch (\Exception $e) {
            Log::error('Error during login', ['error' => $e->getMessage()]);

            return response()->json([
                'status' => false,
                'message' => 'Internal Server Error',
            ], 500);
        }
    }

    public function logout(Request $request)
    {
        try {
            $request->user()->currentAccessToken()->delete();

            return response()->json([
                'status' => true,
                'message' => 'Logout successful',
            ]);
        } catch (\Exception $e) {
            Log::error('Error during logout', ['error' => $e->getMessage()]);

            return response()->json([
                'status' => false,
                'message' => 'Internal Server Error',
            ], 500);
        }
    }
}
