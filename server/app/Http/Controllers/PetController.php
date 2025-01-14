<?php

namespace App\Http\Controllers;

use App\Models\Pet;
use CloudinaryLabs\CloudinaryLaravel\Facades\Cloudinary;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Log;

class PetController extends Controller
{
    public function index(Request $request)
    {

        $type = $request->query('type');
        if ($type) {
            $pets = Pet::where('type', $type)->latest()->get();
        } else {
            $pets = Pet::latest()->get();
        }

        if ($pets->isEmpty()) {
            return response()->json([
                'status' => false,
                'message' => $type ? "No pets found for type: $type" : 'No pets found',
            ], 404);
        }

        return response()->json([
            'status' => true,
            'message' => 'Pets retrieved successfully',
            'data' => $pets,
        ]);
    }


    public function show($id)
    {
        $pet = Pet::find($id);

        if (!$pet) {
            return response()->json([
                'status' => false,
                'message' => 'Pet not found'
            ], 404);
        }

        return response()->json($pet);
    }


    public function store(Request $request)
    {
        try {
            $request->validate([
                'name' => 'required|string|max:255',
                'type' => 'required|string|max:255',
                'description' => 'required|string',
                'image' => 'required|image|mimes:jpeg,png,jpg|max:2048'
            ]);

            if ($request->hasFile('image')) {
                try {
                    $cloudinaryImage = $request->file('image')->storeOnCloudinary('adoptme');
                    $url = $cloudinaryImage->getSecurePath();
                    $publicId = $cloudinaryImage->getPublicId();
                } catch (\Exception $e) {
                    Log::error('Error uploading image to Cloudinary: ' . $e->getMessage());
                    return response()->json([
                        'status' => false,
                        'message' => 'Failed to upload image'
                    ], 500);
                }
            }

            $pet = Pet::create([
                'name' => $request->name,
                'type' => $request->type,
                'description' => $request->description,
                'image' => $url,
                'image_public_id' => $publicId
            ]);

            return response()->json([
                'status' => true,
                'message' => 'Pet created successfully',
                'data' => $pet
            ], 201);
        } catch (\Illuminate\Validation\ValidationException $e) {
            return response()->json([
                'status' => false,
                'message' => 'Validation error',
                'errors' => $e->errors()
            ], 422);
        } catch (\Exception $e) {
            Log::error('Error creating pet: ' . $e->getMessage());
            return response()->json([
                'status' => false,
                'message' => 'Internal Server Error'
            ], 500);
        }
    }


    public function update(Request $request, $id)
    {
        Log::info('Received request: ', $request->all());

        try {
            $pet = Pet::find($id);

            if (!$pet) {
                return response()->json([
                    'status' => false,
                    'message' => 'Pet not found'
                ], 404);
            }

            $request->validate([
                'name' => 'required|string|max:255',
                'type' => 'required|string|max:255',
                'description' => 'required|string',
                'image' => 'nullable|image|mimes:jpeg,png,jpg|max:2048',
            ]);

            $url = $pet->image;
            $publicId = $pet->image_public_id;

            if ($request->hasFile('image')) {
                if (!empty($publicId)) {
                    $response = Cloudinary::destroy($publicId);

                    if ($response['result'] !== 'ok') {
                        Log::error("Failed to delete old image from Cloudinary: " . json_encode($response));
                    }
                }

                $cloudinaryImage = $request->file('image')->storeOnCloudinary('adoptme');
                $url = $cloudinaryImage->getSecurePath();
                $publicId = $cloudinaryImage->getPublicId();
            }

            $pet->update([
                'name' => $request->name,
                'type' => $request->type,
                'description' => $request->description,
                'image' => $url,
                'image_public_id' => $publicId,
            ]);

            return response()->json([
                'status' => true,
                'message' => 'Pet updated successfully',
                'data' => $pet
            ]);
        } catch (\Exception $e) {
            Log::error('Error updating pet: ' . $e->getMessage());
            return response()->json([
                'status' => false,
                'message' => 'Internal Server Error'
            ], 500);
        }
    }


    public function destroy(Pet $pet)
    {
        try {
            $imagePublicId = $pet->image_public_id;
            $isDeleted = Cloudinary::destroy($imagePublicId);

            if (!$imagePublicId) {
                return response()->json([
                    'status' => false,
                    'message' => 'Image not found'
                ], 404);
            }

            if (!$isDeleted) {
                return response()->json([
                    'status' => false,
                    'message' => 'Failed to delete image'
                ], 500);
            }

            $pet->delete();
            return response()->json([
                'status' => true,
                'message' => 'Pet deleted successfully',
                'data' => $pet
            ]);
        } catch (\Exception $e) {
            Log::error('Error deleting pet: ' . $e->getMessage());
            return response()->json([
                'status' => false,
                'message' => 'Internal Server Error'
            ], 500);
        }
    }
}
