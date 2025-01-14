<?php

use App\Http\Controllers\AuthController;
use App\Http\Controllers\BookingController;
use App\Http\Controllers\PetController;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;

Route::get('pets', [PetController::class, 'index']);
Route::get('pets/{id}', [PetController::class, 'show']);
Route::post('pets', [PetController::class, 'store'])->middleware(['auth:sanctum', 'admin']);
Route::put('pets/{id}', [PetController::class, 'update'])->middleware(['auth:sanctum', 'admin']);
Route::delete('pets/{pet}', [PetController::class, 'destroy'])->middleware(['auth:sanctum', 'admin']);

Route::post('pets/booking', [BookingController::class, 'store'])->middleware(['auth:sanctum']);

Route::post('register', [AuthController::class, 'register']);
Route::post('login', [AuthController::class, 'login']);
Route::middleware('auth:sanctum')->post('logout', [AuthController::class, 'logout']);
