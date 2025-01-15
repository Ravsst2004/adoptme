<?php

use App\Http\Controllers\AuthController;
use App\Http\Controllers\BookingController;
use App\Http\Controllers\PetController;
use App\Http\Controllers\UserController;
use Illuminate\Support\Facades\Route;

Route::get('users/{id}', [UserController::class, 'show']);

Route::get('pets', [PetController::class, 'index']);
Route::get('pets/{id}', [PetController::class, 'show']);
Route::post('pets', [PetController::class, 'store'])->middleware(['auth:sanctum', 'admin']);
Route::put('pets/{id}', [PetController::class, 'update'])->middleware(['auth:sanctum', 'admin']);
Route::delete('pets/{pet}', [PetController::class, 'destroy'])->middleware(['auth:sanctum', 'admin']);

Route::get('bookings', [BookingController::class, 'index'])->middleware(['auth:sanctum', 'admin']);
Route::get('booking', [BookingController::class, 'show'])->middleware(['auth:sanctum']);
Route::post('booking', [BookingController::class, 'store'])->middleware(['auth:sanctum']);
Route::put('booking/{id}/status', [BookingController::class, 'updateStatus'])->middleware(['auth:sanctum', 'admin']);
Route::delete('booking/{booking}', [BookingController::class, 'destroy'])->middleware(['auth:sanctum']);

Route::post('register', [AuthController::class, 'register']);
Route::post('login', [AuthController::class, 'login']);
Route::middleware('auth:sanctum')->post('logout', [AuthController::class, 'logout']);
