<?php

use App\Http\Controllers\PetController;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;

Route::resource('pets', PetController::class)->only(['index', 'store', 'show', 'update', 'destroy']);
